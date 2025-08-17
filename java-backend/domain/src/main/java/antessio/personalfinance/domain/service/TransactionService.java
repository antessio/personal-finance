package antessio.personalfinance.domain.service;

import antessio.personalfinance.common.DateProvider;
import antessio.personalfinance.domain.dto.*;
import antessio.personalfinance.domain.events.*;
import antessio.personalfinance.domain.model.*;
import antessio.personalfinance.domain.ports.TransactionEventPublisher;
import antessio.personalfinance.domain.ports.TransactionRepository;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TransactionService {

    private static final float PERFECT_SIMILARITY_THRESHOLD = 0.8f;
    private static final float PARTIAL_SIMILARITY_THRESHOLD = 0.5f;


    private final TransactionRepository transactionRepository;
    private final DateProvider dateProvider;
    private final CategoryService categoryService;
    private final BudgetService budgetService;
    private final TransactionEventPublisher transactionEventPublisher;


    public TransactionService(TransactionRepository transactionRepository, DateProvider dateProvider, CategoryService categoryService, BudgetService budgetService, TransactionEventPublisher transactionEventPublisher) {
        this.transactionRepository = transactionRepository;
        this.dateProvider = dateProvider;
        this.categoryService = categoryService;
        this.budgetService = budgetService;
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
        List<Transaction> transactions = transactionRepository.findAllByUserAndFilters(userOwner, 10000, null, null, null, null, null)
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

    private List<Transaction> getAllTransactionsForExport(YearMonth yearMonth, String
            username, List<CategoryId> categoryIds, TransactionId startingAfterId) {
        return transactionRepository.findAllByUserAndFilters(
                username,
                300,
                yearMonth,
                false,
                null,
                categoryIds,
                startingAfterId);
    }

    private static SavingsExportDTO convertToExportSavings(Transaction
                                                                   t, Map<CategoryId, CategoryDTO> categories) {
        Optional<CategoryDTO> maybeCategory = Optional.ofNullable(t.getCategoryId())
                .flatMap(cId -> Optional.ofNullable(categories.get(cId)));

        return SavingsExportDTO.builder()
                .date(t.getDate())
                .category(maybeCategory.map(TransactionService::convertCategoryForExport).orElse(""))
                .currency("€")
                .amount(String.format(Locale.US, "\"%.2f\"", (t.getAmount().doubleValue() * -1)))
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
                .description("\"" + t.getDescription() + "\"")
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

    public List<MonthlyDataDTO> getMonthlyBudgets(String username, LocalDate startDate, LocalDate endDate) {

        Map<YearMonth, List<Transaction>> transactionsByMonth = transactionRepository.findAllIncludedCategorizedByUserAndYear(
                        username,
                        startDate, endDate)
                .collect(Collectors.groupingBy(transaction -> YearMonth.from(transaction.getDate())));

        Map<CategoryId, CategoryDTO> categories = categoryService.getAllCategories(username, Integer.MAX_VALUE, null)
                .stream()
                .collect(Collectors.toMap(CategoryDTO::getId, Function.identity()));
        return transactionsByMonth
                .entrySet()
                .stream()
                .map(entry -> {
                    YearMonth yearMonth = entry.getKey();
                    List<Transaction> transactions = entry.getValue();
                    double totalIncome = transactions.stream()
                            .filter(t -> Optional.ofNullable(categories.get(t.getCategoryId()))
                                    .map(CategoryDTO::getMacroCategory)
                                    .map(MacroCategoryEnum::isIncome)
                                    .orElse(false))
                            .mapToDouble(t -> t.getAmount().doubleValue())
                            .sum();
                    double totalExpenses = transactions.stream()
                            .filter(t -> Optional.ofNullable(categories.get(t.getCategoryId()))
                                    .map(CategoryDTO::getMacroCategory)
                                    .map(MacroCategoryEnum::isExpense)
                                    .orElse(false))
                            .mapToDouble(t -> t.getAmount().doubleValue())
                            .sum();
                    double totalSavings = transactions.stream()
                            .filter(t -> Optional.ofNullable(categories.get(t.getCategoryId()))
                                    .map(CategoryDTO::getMacroCategory)
                                    .map(MacroCategoryEnum::isSavings)
                                    .orElse(false))

                            .mapToDouble(t -> t.getAmount().doubleValue())
                            .sum();
                    return new MonthlyDataDTO(yearMonth, BigDecimal.valueOf(totalIncome).abs(), BigDecimal.valueOf(totalExpenses).abs(), BigDecimal.valueOf(totalSavings).abs());
                })
                .sorted(Comparator.comparing(MonthlyDataDTO::yearMonth))
                .toList();
    }

    public List<CategorySpendingDTO> getCategorySpending(String username, LocalDate fromDate, LocalDate toDate) {
        Map<CategoryId, CategoryDTO> categories = categoryService.getAllCategories(username, Integer.MAX_VALUE, null)
                .stream()
                .filter(c -> c.getMacroCategory().isExpense())
                .collect(Collectors.toMap(CategoryDTO::getId, Function.identity()));

        Integer year = fromDate.getYear();
        if (toDate.getYear() != year) {
            throw new IllegalArgumentException("From and to date must be in the same year");
        }
        Map<CategoryId, BigDecimal> budgetTotals = budgetService.getBudgetsTotals(username, year);
        Map<CategoryId, Double> spending = transactionRepository.findAllByUserAndYearAndCategories(
                        username,
                        fromDate, toDate, categories.keySet().stream().toList())
                .collect(Collectors.groupingBy(
                        Transaction::getCategoryId,
                        Collectors.summingDouble(t -> t.getAmount().doubleValue())));

        return spending.entrySet()
                .stream()
                .map(s -> new CategorySpendingDTO(categories.get(s.getKey()),
                        BigDecimal.valueOf(s.getValue()).abs(),
                        budgetTotals.get(s.getKey())))
                .toList();
    }

    public BigDecimal getTotalSavings(String username, LocalDate fromDate, LocalDate toDate) {
        List<CategoryId> categories = categoryService.getAllCategories(username, 1000, null)
                .stream()
                .filter(c -> c.getMacroCategory().isSavings())
                .map(CategoryDTO::getId)
                .toList();
        return transactionRepository.findAllByUserAndYearAndCategories(username, fromDate, toDate, categories)
                .reduce(BigDecimal.ZERO,
                        (total, transaction) -> total.add(transaction.getAmount()),
                        BigDecimal::add).abs();
    }

    public BigDecimal getTotalExpenses(String username, LocalDate fromDate, LocalDate toDate) {
        List<CategoryId> categories = categoryService.getAllCategories(username, 1000, null)
                .stream()
                .filter(c -> c.getMacroCategory().isExpense())
                .map(CategoryDTO::getId)
                .toList();
        return transactionRepository.findAllByUserAndYearAndCategories(username, fromDate, toDate, categories)
                .reduce(BigDecimal.ZERO,
                        (total, transaction) -> total.add(transaction.getAmount()),
                        BigDecimal::add).abs();
    }
    public BigDecimal getTotalIncome(String username, LocalDate fromDate, LocalDate toDate) {
        List<CategoryId> categories = categoryService.getAllCategories(username, Integer.MAX_VALUE, null)
                .stream()
                .filter(c -> c.getMacroCategory().isIncome())
                .map(CategoryDTO::getId)
                .toList();
        return transactionRepository.findAllByUserAndYearAndCategories(username, fromDate, toDate, categories)
                .reduce(BigDecimal.ZERO,
                        (total, transaction) -> total.add(transaction.getAmount()),
                        BigDecimal::add).abs();
    }
}
