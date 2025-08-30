package antessio.personalfinance.domain.service;

import antessio.personalfinance.domain.dto.*;
import antessio.personalfinance.domain.model.*;
import antessio.personalfinance.domain.ports.TransactionRepository;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TransactionQueryService {


    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final BudgetService budgetService;


    public TransactionQueryService(TransactionRepository transactionRepository,
                                   CategoryService categoryService, BudgetService budgetService) {
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
        this.budgetService = budgetService;
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


    public List<AccountMonthlyDataDTO> getAccountMonthlyData(String username, LocalDate startDate, LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        if (startDate.isEqual(endDate)) {
            throw new IllegalArgumentException("Start date and end date cannot be the same");
        }
        boolean groupingByMonth = isGroupingByMonth(startDate, endDate);

        if (groupingByMonth) {
            Map<YearMonth, Map<Integer, Map<AccountType, AccountMonthlyDataDTO>>> transactionsByYearMonthAndWeek = transactionRepository.findAllIncludedCategorizedByUserAndYear(username, startDate, endDate)
                    .map(t -> {
                        AccountType accountType = AccountType.fromString(t.getSource());
                        YearMonth yearMonth = YearMonth.from(t.getDate());
                        int week = t.getDate().get(WeekFields.ISO.weekOfMonth());

                        return new AccountMonthlyDataDTO(yearMonth.getYear(), yearMonth.getMonthValue(), week, accountType, t.getAmount());
                    })
                    .collect(Collectors.groupingBy(a -> YearMonth.of(a.year(), a.month()),
                            Collectors.groupingBy(AccountMonthlyDataDTO::week,
                                    Collectors.groupingBy(AccountMonthlyDataDTO::accountType,
                                            Collectors.reducing(new AccountMonthlyDataDTO(0, 0, 0, null, BigDecimal.ZERO),
                                                    Function.identity(),
                                                    (a, b) -> new AccountMonthlyDataDTO(a.year(), a.month(), a.week(), Optional.ofNullable(a.accountType()).orElse(b.accountType()),
                                                            a.total().add(b.total())))))));

            return transactionsByYearMonthAndWeek.entrySet().stream()
                    .flatMap(yearEntry -> yearEntry.getValue().entrySet().stream()
                            .flatMap(weekEntry -> weekEntry.getValue().values().stream()
                                    .map(data -> new AccountMonthlyDataDTO(yearEntry.getKey().getYear(), yearEntry.getKey().getMonthValue(), weekEntry.getKey(), data.accountType(), data.total()))))
                    .sorted(Comparator.comparing(AccountMonthlyDataDTO::year))
                    .toList();
        } else {

            Map<YearMonth, Map<AccountType, AccountMonthlyDataDTO>> result = new HashMap<>();
            transactionRepository.findAllIncludedCategorizedByUserAndYear(
                            username,
                            startDate, endDate)
                    .map(t -> {

                        AccountType accountType = AccountType.fromString(t.getSource());
                        YearMonth yearMonth = YearMonth.from(t.getDate());
                        int week = t.getDate().get(WeekFields.ISO.weekOfMonth());

                        return new AccountMonthlyDataDTO(yearMonth.getYear(), yearMonth.getMonthValue(), week, accountType, t.getAmount());
                    })
                    .forEach(data -> {
                        YearMonth key = YearMonth.of(data.year(), data.month());
                        if (!result.containsKey(key)) {
                            result.put(key, new EnumMap<>(AccountType.class));
                        }
                        Map<AccountType, AccountMonthlyDataDTO> accountTypeMap = result.get(key);
                        if (accountTypeMap.containsKey(data.accountType())) {
                            accountTypeMap.computeIfPresent(data.accountType(),
                                    (k, existing) -> new AccountMonthlyDataDTO(existing.year(), existing.month(), 0, existing.accountType(), existing.total().add(data.total())));
                        } else {
                            accountTypeMap.put(data.accountType(), new AccountMonthlyDataDTO(data.year(), data.month(), 0, data.accountType(), data.total()));
                        }


                    });
            return result.values().stream().flatMap(m -> m.values().stream()).toList();
        }

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
                .category(maybeCategory.map(TransactionQueryService::convertCategoryForExport).orElse(""))
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
                .category(maybeCategory.map(TransactionQueryService::convertCategoryForExport).orElse(""))
                .macroCategory(maybeCategory.map(TransactionQueryService::convertMacroCategoryForExport).orElse(""))
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
        boolean groupingByMonth = isGroupingByMonth(startDate, endDate);
        Map<YearMonth, Map<Integer, List<Transaction>>> transactionsByMonth = transactionRepository.findAllIncludedCategorizedByUserAndYear(
                        username,
                        startDate, endDate)
                .collect(Collectors.groupingBy(transaction -> YearMonth.from(transaction.getDate()),
                        Collectors.groupingBy(transaction -> ((transaction.getDate().getDayOfMonth()) / 7) + 1, Collectors.toList())));

        Map<CategoryId, CategoryDTO> categories = categoryService.getAllCategories(username, Integer.MAX_VALUE, null)
                .stream()
                .collect(Collectors.toMap(CategoryDTO::getId, Function.identity()));
        return transactionsByMonth
                .entrySet()
                .stream()
                .flatMap(entry -> {
                    YearMonth yearMonth = entry.getKey();
                    if (groupingByMonth) {
                        return entry
                                .getValue()
                                .entrySet()
                                .stream()
                                .map(e -> {
                                    int week = e.getKey();
                                    double totalIncome = e.getValue()
                                            .stream()
                                            .filter(t -> Optional.ofNullable(categories.get(t.getCategoryId()))
                                                    .map(CategoryDTO::getMacroCategory)
                                                    .map(MacroCategoryEnum::isIncome)
                                                    .orElse(false))
                                            .mapToDouble(t -> t.getAmount().doubleValue())
                                            .sum();
                                    double totalExpenses = e.getValue()
                                            .stream()
                                            .filter(t -> Optional.ofNullable(categories.get(t.getCategoryId()))
                                                    .map(CategoryDTO::getMacroCategory)
                                                    .map(MacroCategoryEnum::isExpense)
                                                    .orElse(false))
                                            .mapToDouble(t -> t.getAmount().doubleValue())
                                            .sum();
                                    double totalSavings = e.getValue()
                                            .stream()
                                            .filter(t -> Optional.ofNullable(categories.get(t.getCategoryId()))
                                                    .map(CategoryDTO::getMacroCategory)
                                                    .map(MacroCategoryEnum::isSavings)
                                                    .orElse(false))
                                            .mapToDouble(t -> t.getAmount().doubleValue())
                                            .sum();
                                    return new MonthlyDataDTO(yearMonth, week,
                                            BigDecimal.valueOf(totalIncome).abs(),
                                            BigDecimal.valueOf(totalExpenses).abs(),
                                            BigDecimal.valueOf(totalSavings).abs());
                                });

                    } else {

                        List<Transaction> transactions = entry
                                .getValue()
                                .values()
                                .stream()
                                .flatMap(List::stream)
                                .toList();
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
                        return Stream.of(new MonthlyDataDTO(yearMonth, 0,
                                BigDecimal.valueOf(totalIncome).abs(), BigDecimal.valueOf(totalExpenses).abs(), BigDecimal.valueOf(totalSavings).abs()));
                    }
                })
                .sorted(Comparator.comparing(MonthlyDataDTO::yearMonth))
                .toList();
    }

    private static boolean isGroupingByMonth(LocalDate startDate, LocalDate endDate) {
        return startDate.getYear() == endDate.getYear() && startDate.getMonth() == endDate.getMonth();
    }

    public List<CategorySpendingDTO> getCategorySpending(String username, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        if (startDate.getYear() != endDate.getYear()) {
            throw new IllegalArgumentException("Start date and end date must be in the same year");
        }
        Integer month = startDate.getMonthValue() == endDate.getMonthValue() ? startDate.getMonthValue() : null;
        return getCategorySpending(username, startDate.getYear(), month);

    }

    public List<CategorySpendingDTO> getCategorySpending(String username, int year, Integer month) {
        Map<CategoryId, CategoryDTO> categories = categoryService.getAllCategories(username, Integer.MAX_VALUE, null)
                .stream()
                .filter(c -> c.getMacroCategory().isExpense())
                .collect(Collectors.toMap(CategoryDTO::getId, Function.identity()));


        LocalDate fromDate = LocalDate.of(year, Optional.ofNullable(month).orElse(1), 1);
        LocalDate toDate = month != null ? fromDate.withDayOfMonth(fromDate.lengthOfMonth()) : LocalDate.of(year, 12, 31);

        Map<CategoryId, BigDecimal> budgetTotals = budgetService.getBudgetsTotals(username, year, month);
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

    public List<MacroCategoryMonthlyDataDTO> getExpensesMacroCategoriesMonthlyBudgets(String username, LocalDate startDate, LocalDate endDate) {
        boolean groupingByMonth = isGroupingByMonth(startDate, endDate);
        Map<YearMonth, Map<Integer, List<Transaction>>> transactionsByMonth = transactionRepository.findAllIncludedCategorizedByUserAndYear(
                        username,
                        startDate, endDate)
                .collect(Collectors.groupingBy(transaction -> YearMonth.from(transaction.getDate()),
                        Collectors.groupingBy(transaction -> ((transaction.getDate().getDayOfMonth()) / 7) + 1, Collectors.toList())));

        Map<CategoryId, CategoryDTO> categories = categoryService.getAllCategories(username, Integer.MAX_VALUE, null)
                .stream()
                .collect(Collectors.toMap(CategoryDTO::getId, Function.identity()));
        return transactionsByMonth
                .entrySet()
                .stream()
                .flatMap(entry -> {
                    if (groupingByMonth) {
                        YearMonth yearMonth = entry.getKey();
                        Map<MacroCategoryEnum, Map<Integer, BigDecimal>> partialMap = entry
                                .getValue()
                                .entrySet()
                                .stream()
                                .flatMap(e -> {
                                    int week = e.getKey();
                                    return e.getValue()
                                            .stream()
                                            .filter(t -> categories.get(t.getCategoryId()).getMacroCategory().isExpense())
                                            .map(t -> new MacroCategoryMonthlyDataDTO(
                                                    yearMonth.getYear(),
                                                    yearMonth.getMonthValue(),
                                                    week,
                                                    categories.get(t.getCategoryId()).getMacroCategory(),
                                                    t.getAmount().abs()
                                            ));
                                })
                                .collect(Collectors.groupingBy(MacroCategoryMonthlyDataDTO::macroCategory,
                                        Collectors.groupingBy(MacroCategoryMonthlyDataDTO::week, Collectors.reducing(
                                                BigDecimal.ZERO,
                                                MacroCategoryMonthlyDataDTO::total,
                                                BigDecimal::add
                                        ))));
                        return partialMap.entrySet()
                                .stream()
                                .flatMap(entry1 -> entry1.getValue()
                                        .entrySet()
                                        .stream()
                                        .map(e -> new MacroCategoryMonthlyDataDTO(
                                                yearMonth.getYear(),
                                                yearMonth.getMonthValue(),
                                                e.getKey(),
                                                entry1.getKey(),
                                                e.getValue().abs())));
                    } else {
                        YearMonth yearMonth = entry.getKey();
                        Map<MacroCategoryEnum, BigDecimal> partialsMap = entry.getValue()
                                .values()
                                .stream()
                                .flatMap(Collection::stream)
                                .map(t -> Pair.of(categories.get(t.getCategoryId()).getMacroCategory(), t))
                                .filter(p -> p.getLeft().isExpense())
                                .map(p -> new MacroCategoryMonthlyDataDTO(
                                        yearMonth.getYear(),
                                        yearMonth.getMonthValue(),
                                        p.getRight().getDate().get(WeekFields.of(Locale.getDefault()).weekOfMonth()),
                                        p.getLeft(),
                                        p.getRight().getAmount()))
                                .collect(Collectors.groupingBy(
                                        MacroCategoryMonthlyDataDTO::macroCategory,
                                        Collectors.reducing(
                                                BigDecimal.ZERO,
                                                MacroCategoryMonthlyDataDTO::total,
                                                BigDecimal::add
                                        )));

                        return partialsMap.entrySet()
                                .stream()
                                .map(e -> new MacroCategoryMonthlyDataDTO(
                                        yearMonth.getYear(),
                                        yearMonth.getMonthValue(),
                                        0, // week is not applicable when grouping by month
                                        e.getKey(),
                                        e.getValue().abs()));

                    }
                })
                .sorted(Comparator.comparing(MacroCategoryMonthlyDataDTO::macroCategory).thenComparing(MacroCategoryMonthlyDataDTO::month).thenComparing(MacroCategoryMonthlyDataDTO::week))
                .toList();
    }

    public List<CategoryMonthlyDataDTO> getCategoryMonthlyData(String username, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        if (startDate.isEqual(endDate)) {
            throw new IllegalArgumentException("Start date and end date cannot be the same");
        }
        boolean groupingByMonth = isGroupingByMonth(startDate, endDate);
        Map<CategoryId, CategoryDTO> categories = categoryService.getAllCategories(username, Integer.MAX_VALUE, null)
                .stream()
                .filter(c -> c.getMacroCategory().isExpense())
                .collect(Collectors.toMap(CategoryDTO::getId, Function.identity()));
        Stream<CategoryMonthlyDataDTO> categoryMonthlyDataDTOStream = transactionRepository.findAllIncludedCategorizedByUserAndYear(username, startDate, endDate)
                .filter(t -> t.getCategoryId() != null && categories.containsKey(t.getCategoryId()))
                .map(t -> {
                    YearMonth yearMonth = YearMonth.from(t.getDate());
                    int week = t.getDate().get(WeekFields.ISO.weekOfMonth());

                    return new CategoryMonthlyDataDTO(categories.get(t.getCategoryId()),
                            week,
                            yearMonth.getYear(),
                            yearMonth.getMonthValue(),
                            t.getAmount());
                });
        if (groupingByMonth) {
            Map<YearMonth, Map<Integer, Map<CategoryId, CategoryMonthlyDataDTO>>> transactionsByYearMonthAndWeek = categoryMonthlyDataDTOStream
                    .collect(Collectors.groupingBy(a -> YearMonth.of(a.year(), a.month()),
                            Collectors.groupingBy(CategoryMonthlyDataDTO::week,
                                    Collectors.groupingBy(d -> d.category().getId(),
                                            Collectors.reducing(new CategoryMonthlyDataDTO(null, 0, 0, null, BigDecimal.ZERO),
                                                    Function.identity(),
                                                    (a, b) -> new CategoryMonthlyDataDTO(a.category(), a.week(), a.year(), a.month(),
                                                            a.total().add(b.total())))))));

            return transactionsByYearMonthAndWeek.entrySet().stream()
                    .flatMap(yearEntry -> yearEntry.getValue().entrySet().stream()
                            .flatMap(weekEntry -> weekEntry.getValue().values().stream()))
                    .map(t -> new CategoryMonthlyDataDTO(t.category(), t.week(), t.year(), t.month(), t.total().abs()))
                    .sorted(Comparator.comparing(CategoryMonthlyDataDTO::year))
                    .toList();
        } else {

            Map<YearMonth, Map<CategoryId, CategoryMonthlyDataDTO>> result = new HashMap<>();
            categoryMonthlyDataDTOStream
                    .forEach(data -> {
                        YearMonth key = YearMonth.of(data.year(), data.month());
                        if (!result.containsKey(key)) {
                            result.put(key, new HashMap<>());
                        }
                        Map<CategoryId, CategoryMonthlyDataDTO> accountTypeMap = result.get(key);
                        if (accountTypeMap.containsKey(data.category().getId())) {
                            accountTypeMap.computeIfPresent(data.category().getId(),
                                    (k, existing) -> new CategoryMonthlyDataDTO(existing.category(),
                                            0,
                                            existing.year(),
                                            existing.month(),
                                            existing.total().add(data.total())));
                        } else {
                            accountTypeMap.put(data.category().getId(), new CategoryMonthlyDataDTO(data.category(), 0, data.year(), data.month(), data.total()));
                        }


                    });
            return result.values().stream().flatMap(m -> m.values().stream())
                    .map(t -> new CategoryMonthlyDataDTO(t.category(), t.week(), t.year(), t.month(), t.total().abs())).toList();
        }
    }
}
