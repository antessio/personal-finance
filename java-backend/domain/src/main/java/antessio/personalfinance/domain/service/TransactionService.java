package antessio.personalfinance.domain.service;

import antessio.personalfinance.common.DateProvider;
import antessio.personalfinance.domain.dto.*;
import antessio.personalfinance.domain.events.*;
import antessio.personalfinance.domain.model.*;
import antessio.personalfinance.domain.ports.TransactionEventPublisher;
import antessio.personalfinance.domain.ports.TransactionRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final DateProvider dateProvider;
    private final CategoryService categoryService;
    private final TransactionEventPublisher transactionEventPublisher;

    public TransactionService(TransactionRepository transactionRepository, DateProvider dateProvider, CategoryService categoryService, TransactionEventPublisher transactionEventPublisher) {
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
        transactionRepository.update(transaction);
        if (!events.isEmpty()) {
            transactionEventPublisher.publish(events);
        }
    }

    public void processCategoriesAll(String userOwner) {
        List<TransactionEvent> events = new ArrayList<>();
        List<Transaction> transactions = transactionRepository.findAllByUserAndFilters(userOwner, 10000, null, null, null, null, null)
                .stream()
                .map(t -> updateTransactionAndGetEvents(userOwner, t, events))
                .toList();
        transactionRepository.updateAll(transactions);
        transactionEventPublisher.publish(events);
    }

    private Transaction updateTransactionAndGetEvents(String userOwner, Transaction t, List<TransactionEvent> events) {
        Optional<AutomaticSkip> automaticSkip = categoryService.getAutomaticSkip(userOwner);
        if (automaticSkip.map(a -> a.hasToSkip(t.getDescription())).orElse(false)) {
            t.skip();
            events.add(new TransactionSkipped(t.getId()));
        }
        return categoryService.findMatchingCategory(userOwner, t.getDescription())
                .map(c -> {
                    this.assignCategory(c.getId(), t);
                    events.add(new TransactionCategoryAssigned(t.getId(), c.getId()));
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

    public List<TransactionDTO> findTransactions(TransactionsQueryDTO query) {
        List<Transaction> allByUserAndFilters = transactionRepository.findAllByUserAndFilters(
                query.getUserOwner(),
                query.getLimit(),
                query.getMonth().orElse(null),
                query.getSkip().orElse(null),
                query.getSource().orElse(null),
                query.getUncategorized().orElse(false) ?
                        List.of() :
                        query.getCategoryId()
                                .map(List::of)
                                .orElse(null),
                query.getCursor().orElse(null)

        );
        Map<CategoryId, CategoryDTO> categoryIdCategoryDTOMap = categoryService.getCategoriesByIdsAndUser(
                        allByUserAndFilters.stream().map(Transaction::getCategoryId).filter(Objects::nonNull).toList(),
                        query.getUserOwner())
                .stream()
                .collect(Collectors.toMap(CategoryDTO::getId, Function.identity()));
        return allByUserAndFilters
                .stream()
                .map(t -> toDTO(t, tx -> categoryIdCategoryDTOMap.get(tx.getCategoryId())))
                .toList();
    }

    private Transaction fromDTO(CreateTransactionDTO createTransactionDTO) {
        return new Transaction(
                TransactionId.generate(),
                createTransactionDTO.date(),
                createTransactionDTO.amount(),
                createTransactionDTO.description(),
                generateUniqueId(createTransactionDTO.date().toString(), createTransactionDTO.amount().doubleValue(), createTransactionDTO.description()),
                createTransactionDTO.source(),
                false,
                createTransactionDTO.userOwner(),
                null,
                dateProvider.getLocalDateTimeNow(),
                null
        );
    }

    private Stream<Transaction> findOwnedTransactions(List<TransactionId> transactionIds, String userOwner) {
        return transactionRepository.findByIds(transactionIds)
                .stream()
                .filter(t -> t.getUserOwner().equals(userOwner));
    }


    private String generateUniqueId(String date, double amount, String description) {
        try {
            String uniqueString = date + "-" + amount + "-" + description;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(uniqueString.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Errore durante la generazione dell'hash", e);
        }
    }

    private TransactionDTO toDTO(Transaction transaction,
                                 Function<Transaction, CategoryDTO> categoryResolution) {
        return new TransactionDTO(
                transaction.getId(),
                transaction.getDate(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getSource(),
                transaction.getSkip(),
                transaction.getUserOwner(),
                categoryResolution.apply(transaction),
                transaction.getInsertedAt(),
                transaction.getUpdatedAt()
        );
    }

    public List<SavingsExportDTO> exportSavings(YearMonth yearMonth, String username) {
        Map<CategoryId, CategoryDTO> categories = categoryService.getAllCategories(username, 1000, null)
                .stream()
                .filter(c -> c.getMacroCategory() == MacroCategoryEnum.SAVINGS)
                .collect(Collectors.toMap(CategoryDTO::getId, Function.identity()));
        List<CategoryId> categoryIds = categories.values().stream().map(CategoryDTO::getId).toList();
        List<Transaction> transactions = getAllTransactionsForExport(yearMonth, username, categoryIds, null);
        return Stream.iterate(transactions,
                        transactionList -> !transactionList.isEmpty(),
                        transactionList -> getAllTransactionsForExport(yearMonth, username, categoryIds, transactionList.getLast().getId()))
                .flatMap(List::stream)
                .sorted(Comparator.comparing(Transaction::getDate))
                .map(t -> convertToExportSavings(t, categories))
                .toList();

    }

    public List<TransactionExportDTO> exportTransactions(YearMonth yearMonth, String username) {
        Map<CategoryId, CategoryDTO> categories = categoryService.getAllCategories(username, 1000, null)
                .stream()
                .filter(c -> c.getMacroCategory() != MacroCategoryEnum.SAVINGS)
                .collect(Collectors.toMap(CategoryDTO::getId, Function.identity()));
        List<CategoryId> categoryIds = categories.values().stream().map(CategoryDTO::getId).toList();
        List<Transaction> transactions = getAllTransactionsForExport(yearMonth, username, categoryIds, null);

        return Stream.iterate(transactions,
                        transactionList -> !transactionList.isEmpty(),
                        transactionList -> getAllTransactionsForExport(yearMonth, username, categoryIds, transactionList.getLast().getId()))
                .flatMap(List::stream)
                .sorted(Comparator.comparing(Transaction::getDate))
                .map(t -> convertToExport(t, categories))
                .toList();

    }

    private List<Transaction> getAllTransactionsForExport(YearMonth yearMonth, String username, List<CategoryId> categoryIds, TransactionId startingAfterId) {
        return transactionRepository.findAllByUserAndFilters(
                username,
                300,
                yearMonth,
                false,
                null,
                categoryIds,
                startingAfterId);
    }

    private static SavingsExportDTO convertToExportSavings(Transaction t, Map<CategoryId, CategoryDTO> categories) {
        Optional<CategoryDTO> maybeCategory = Optional.ofNullable(t.getCategoryId())
                .flatMap(cId -> Optional.ofNullable(categories.get(cId)));

        return SavingsExportDTO.builder()
                .date(t.getDate())
                .category(maybeCategory.map(TransactionService::convertCategoryForExport).orElse(""))
                .currency("€")
                .amount(String.format(Locale.US, "\"%.2f\"", (t.getAmount().doubleValue() * -1) ))
                .build();
    }
    private static TransactionExportDTO convertToExport(Transaction t, Map<CategoryId, CategoryDTO> categories) {
        Optional<CategoryDTO> maybeCategory = Optional.ofNullable(t.getCategoryId())
                .flatMap(cId -> Optional.ofNullable(categories.get(cId)));

        return TransactionExportDTO.builder()
                .date(t.getDate())
                .type(t.getAmount().doubleValue() > 0 ? "Income" : "Expense")
                .category(maybeCategory.map(TransactionService::convertCategoryForExport).orElse(""))
                .macroCategory(maybeCategory.map(TransactionService::convertMacroCategoryForExport).orElse(""))
                .currency("€")
                .amount(String.format(Locale.US, "\"%.2f\"", Math.abs(t.getAmount().doubleValue())))
                .description("\""+t.getDescription()+"\"")
                .build();
    }
    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private static String convertMacroCategoryForExport(CategoryDTO categoryDTO) {
        if (categoryDTO.getMacroCategory() == MacroCategoryEnum.EXPENSE) {
            return "Expense Var.";
        } else {
            return capitalize(categoryDTO.getMacroCategory().name());
        }
    }

    private static String convertCategoryForExport(CategoryDTO categoryDTO) {
        return categoryDTO.getEmoji() + " " + categoryDTO.getName().toUpperCase();
    }
}
