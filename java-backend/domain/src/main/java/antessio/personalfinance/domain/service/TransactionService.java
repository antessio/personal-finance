package antessio.personalfinance.domain.service;

import antessio.personalfinance.common.DateProvider;
import antessio.personalfinance.domain.dto.*;
import antessio.personalfinance.domain.events.*;
import antessio.personalfinance.domain.model.*;
import antessio.personalfinance.domain.ports.TransactionEventPublisher;
import antessio.personalfinance.domain.ports.TransactionRepository;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Stream;

public class TransactionService {

    private static final float PERFECT_SIMILARITY_THRESHOLD = 0.8f;
    private static final float PARTIAL_SIMILARITY_THRESHOLD = 0.5f;


    private final TransactionRepository transactionRepository;
    private final DateProvider dateProvider;
    private final CategoryService categoryService;
    private final TransactionEventPublisher transactionEventPublisher;


    public TransactionService(TransactionRepository transactionRepository, DateProvider dateProvider,
                              CategoryService categoryService,
                              TransactionEventPublisher transactionEventPublisher) {
        this.transactionRepository = transactionRepository;
        this.dateProvider = dateProvider;
        this.categoryService = categoryService;
        this.transactionEventPublisher = transactionEventPublisher;
    }


    public void createTransactions(List<CreateTransactionDTO> createTransactionDTOs) {
        List<Transaction> transactions = createTransactionDTOs
                .stream()
                .map(this::fromDTO)
                .toList();
        transactionRepository.saveAll(transactions)
                .stream()
                .map(Transaction::getId)
                .map(TransactionCreated::new)
                .forEach(transactionEventPublisher::publish);

    }

    public void skipTransactions(List<TransactionId> transactionIds, String userOwner) {
        List<Transaction> skippedTransactions = findOwnedTransactions(transactionIds, userOwner)
                .peek(Transaction::skip)
                .toList();
        transactionRepository.updateAll(skippedTransactions);
        skippedTransactions
                .stream()
                .map(Transaction::getId)
                .map(TransactionSkipped::new)
                .forEach(transactionEventPublisher::publish);
    }

    public void includeTransactions(List<TransactionId> transactionIds, String userOwner) {
        List<Transaction> includedTransactions = findOwnedTransactions(transactionIds, userOwner)
                .peek(Transaction::include)
                .toList();
        transactionRepository.updateAll(includedTransactions);
        includedTransactions
                .stream()
                .map(Transaction::getId)
                .map(TransactionIncluded::new)
                .forEach(transactionEventPublisher::publish);

    }

    public void bulkUpdate(TransactionBulkUpdateDTO bulkUpdate, String userOwner) {

        List<TransactionEvent> events = new ArrayList<>();
        List<Transaction> updatedTransactions = findOwnedTransactions(bulkUpdate.transactionIds(), userOwner)
                .map(t -> {
                    List<TransactionEvent> updateEvents = updateTransaction(bulkUpdate, t);
                    if (!updateEvents.isEmpty()) {
                        events.addAll(updateEvents);
                        return t;
                    } else {
                        return null;
                    }
                })
                .toList();
        transactionRepository.updateAll(updatedTransactions);
        events.forEach(transactionEventPublisher::publish);

    }

    private static List<TransactionEvent> updateTransaction(TransactionBulkUpdateDTO bulkUpdate, Transaction t) {
        List<TransactionEvent> events = new ArrayList<>();
        if (bulkUpdate.skip() != null) {
            if (bulkUpdate.skip()) {
                t.skip();
                events.add(new TransactionSkipped(t.getId()));
            } else {
                t.include();
                events.add(new TransactionIncluded(t.getId()));
            }
        }
        if (bulkUpdate.categoryId() != null) {
            t.assignCategory(bulkUpdate.categoryId());
            events.add(new TransactionCategoryAssigned(t.getId(), bulkUpdate.categoryId()));
        }
        return events;
    }

    public void assignCategory(List<TransactionId> transactionIds, CategoryId categoryId, String userOwner) {
        List<Transaction> transactions = findOwnedTransactions(transactionIds, userOwner)
                .map(t -> assignCategory(categoryId, t))
                .toList();
        transactionRepository.updateAll(transactions);
        transactions
                .stream()
                .map(transaction -> new TransactionCategoryAssigned(transaction.getId(), transaction.getCategoryId()))
                .forEach(transactionEventPublisher::publish);
    }

    private Transaction assignCategory(CategoryId categoryId, Transaction t) {
        t.assignCategory(categoryId);
        return t;
    }

    public void assignCategory(TransactionId id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction %snot found".formatted(id)));
        String userOwner = transaction.getUserOwner();
        Optional<AutomaticSkip> automaticSkip = categoryService.getAutomaticSkip(userOwner);
        List<TransactionEvent> events = new ArrayList<>();
        if (automaticSkip.map(a -> a.hasToSkip(transaction.getDescription())).orElse(false)) {
            transaction.skip();
            events.add(new TransactionSkipped(transaction.getId()));
        }
        categoryService.findMatchingCategory(userOwner, transaction.getDescription())
                .ifPresent(category -> {
                    transaction.assignCategory(category.getId());
                    events.add(new TransactionCategoryAssigned(transaction.getId(), category.getId()));
                });
        if (transaction.getCategoryId() == null && !transaction.isSkipped()) {
            assignCategoryFromSimilarTransaction(transaction, events);

        }
        transactionRepository.update(transaction);
        if (!events.isEmpty()) {
            transactionEventPublisher.publish(events);
        }
    }

    private void assignCategoryFromSimilarTransaction(Transaction transaction, List<TransactionEvent> events) {
        List<Pair<TransactionId, Float>> similarTransactions = transactionRepository.findSimilarTransactionIds(transaction.getUserOwner(),
                transaction.getId(),
                transaction.getDescription());
        if (!similarTransactions.isEmpty()) {
            similarTransactions
                    .stream()
                    .map(pair -> new TransactionSimilarity(transactionRepository.findById(pair.getLeft()).orElseThrow(), pair.getRight()))
                    .filter(t -> t.similarity() > PERFECT_SIMILARITY_THRESHOLD || (t.similarity() > PARTIAL_SIMILARITY_THRESHOLD && t.transaction().getAmount().equals(transaction.getAmount())))
                    .max(Comparator.comparing(TransactionSimilarity::similarity))
                    .map(TransactionSimilarity::transaction)
                    .map(Transaction::getCategoryId)
                    .ifPresent(transactionCategoryId -> {
                        transaction.assignCategory(transactionCategoryId);
                        events.add(new TransactionCategoryAssigned(transaction.getId(), transactionCategoryId));
                    });
        }
    }

    public void processCategoriesAll(String userOwner) {
        List<TransactionEvent> events = new ArrayList<>();
        List<Transaction> transactions = transactionRepository.findAllByUserAndFilters(userOwner, 10000, null, null, null, null, null, null)
                .stream()
                .map(t -> updateTransactionAndGetEvents(userOwner, t, events))
                .toList();
        transactionRepository.updateAll(transactions);
        transactionEventPublisher.publish(events);
    }

    private Transaction updateTransactionAndGetEvents(String userOwner, Transaction
            t, List<TransactionEvent> events) {
        Optional<AutomaticSkip> automaticSkip = categoryService.getAutomaticSkip(userOwner);
        if (automaticSkip.map(a -> a.hasToSkip(t.getDescription())).orElse(false)) {
            t.skip();
            events.add(new TransactionSkipped(t.getId()));
        }
        Optional<CategoryId> maybeMatchingCategory = categoryService.findMatchingCategory(userOwner, t.getDescription())
                .map(Category::getId);
        if (maybeMatchingCategory.isEmpty() && t.getCategoryId() == null && !t.isSkipped()) {
            assignCategoryFromSimilarTransaction(t, events);
        }
        return maybeMatchingCategory
                .map(categoryId -> {
                    this.assignCategory(categoryId, t);
                    events.add(new TransactionCategoryAssigned(t.getId(), categoryId));
                    return t;
                })
                .orElse(t);
    }

    public void processCategories(List<TransactionId> transactionIds, String userOwner) {
        List<TransactionEvent> events = new ArrayList<>();
        List<Transaction> transactions = findOwnedTransactions(transactionIds, userOwner)
                .map(t -> updateTransactionAndGetEvents(userOwner, t, events))
                .toList();
        transactionRepository.updateAll(transactions);
        transactionEventPublisher.publish(events);

    }


    private Transaction fromDTO(CreateTransactionDTO createTransactionDTO) {
        return new Transaction(
                TransactionId.generate(createTransactionDTO.date()),
                createTransactionDTO.date(),
                createTransactionDTO.amount(),
                createTransactionDTO.description(),
                generateUniqueId(createTransactionDTO.date().toString(), createTransactionDTO.amount().doubleValue(), createTransactionDTO.description(), createTransactionDTO.userOwner()),
                createTransactionDTO.source(),
                false,
                createTransactionDTO.userOwner(),
                null,
                dateProvider.getLocalDateTimeNow(),
                null,
                createTransactionDTO.transactionImportId()
        );
    }

    private Stream<Transaction> findOwnedTransactions(List<TransactionId> transactionIds, String userOwner) {
        return transactionRepository.findByIds(transactionIds)
                .stream()
                .filter(t -> t.getUserOwner().equals(userOwner));
    }


    private String generateUniqueId(String date, double amount, String description, String userOwner) {
        try {
            String uniqueString = date + "-" + amount + "-" + description + "-" + userOwner;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(uniqueString.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Errore durante la generazione dell'hash", e);
        }
    }
}
