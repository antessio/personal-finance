package antessio.personalfinance.domain.service;

import antessio.personalfinance.domain.dto.BudgetDTO;
import antessio.personalfinance.domain.dto.CategoryDTO;
import antessio.personalfinance.domain.dto.CreateDefaultBudgetDTO;
import antessio.personalfinance.domain.dto.CreateMonthlyBudgetDTO;
import antessio.personalfinance.domain.model.Budget;
import antessio.personalfinance.domain.model.BudgetId;
import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.ports.BudgetRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryService categoryService;

    public BudgetService(BudgetRepository budgetRepository, CategoryService categoryService) {
        this.budgetRepository = budgetRepository;
        this.categoryService = categoryService;
    }

    public void createMonthlyBudget(String userOwner, CreateMonthlyBudgetDTO createMonthlyBudgetDTO) {
        CategoryDTO category = getUserCategoryById(createMonthlyBudgetDTO.getUserOwner(), createMonthlyBudgetDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found or does not belong to user"));
        YearMonth yearMonth = createMonthlyBudgetDTO.getYearMonth();
        budgetRepository.create(new Budget(BudgetId.generate(), category.getId(), createMonthlyBudgetDTO.getAmount(), userOwner, yearMonth.getYear(), yearMonth.getMonthValue()));
    }

    public Budget createDefaultBudget(String userOwner, CreateDefaultBudgetDTO createDefaultBudgetDTO) {
        CategoryDTO category = getUserCategoryById(createDefaultBudgetDTO.getUserOwner(), createDefaultBudgetDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found or does not belong to user"));
        Budget budget = new Budget(BudgetId.generate(), category.getId(), createDefaultBudgetDTO.getAmount(), userOwner, null, null);
        budgetRepository.create(budget);
        return budget;
    }

    public List<BudgetDTO> createDefaultBudgets(String userOwner, Set<CreateDefaultBudgetDTO> createDefaultBudgetDTOs) {
        List<BudgetId> createdIds = new ArrayList<>();
        Map<CategoryId, Budget> existingBudgets = budgetRepository.getDefaultBudget(userOwner);

        for (CreateDefaultBudgetDTO createDefaultBudgetDTO : createDefaultBudgetDTOs) {
            Budget budget = Optional.ofNullable(existingBudgets.get(createDefaultBudgetDTO.getCategoryId()))
                    .orElseGet(() -> createDefaultBudget(userOwner, createDefaultBudgetDTO));
            createdIds.add(budget.getId());
        }
        return budgetRepository.getByIds(createdIds).stream()
                .map(BudgetDTO::from)
                .toList();

    }

    public Map<CategoryId, BigDecimal> getBudgetsTotals(String owner, int year) {
        return getCategoryIdBigDecimalMap(owner, year, __ -> true);
    }

    public Map<CategoryId, BigDecimal> getBudgetsTotals(String owner, int year, Integer month) {
        if (month == null) {
            return getBudgetsTotals(owner, year);
        } else {
            return getCategoryIdBigDecimalMap(owner, year, month, __ -> true);
        }
    }

    private Map<CategoryId, BigDecimal> getCategoryIdBigDecimalMap(String owner, int year, Integer month, Predicate<CategoryDTO> categoryPredicate) {
        Map<CategoryId, Map<YearMonth, Budget>> monthlyBudgets = budgetRepository.getMonthlyBudgets(owner, year);
        Map<CategoryId, Budget> annualBudgets = budgetRepository.getAnnualBudgets(owner, year);
        Map<CategoryId, Budget> defaultBudgets = budgetRepository.getDefaultBudget(owner);

        return categoryService.getAllCategories(owner)
                .filter(categoryPredicate)
                .map(CategoryDTO::getId)
                .collect(Collectors.toMap(
                        categoryId -> categoryId,
                        categoryId -> Optional.ofNullable(monthlyBudgets.get(categoryId))
                                .map(monthMap -> monthMap.values().stream()
                                        .map(Budget::getAmount)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                                .orElseGet(() -> {
                                            BigDecimal totalBudget = Optional.ofNullable(annualBudgets.get(categoryId))
                                                    .map(Budget::getAmount)
                                                    .orElseGet(() -> Optional.ofNullable(defaultBudgets.get(categoryId))
                                                            .map(Budget::getAmount)
                                                            .orElse(BigDecimal.ZERO));
                                            if (month != null) {
                                                return totalBudget.divide(BigDecimal.valueOf(12L), RoundingMode.HALF_UP);
                                            } else {
                                                return totalBudget;
                                            }
                                        }
                                )));
    }

    private Map<CategoryId, BigDecimal> getCategoryIdBigDecimalMap(String owner, int year, Predicate<CategoryDTO> categoryPredicate) {
        Map<CategoryId, Map<YearMonth, Budget>> monthlyBudgets = budgetRepository.getMonthlyBudgets(owner, year);
        Map<CategoryId, Budget> annualBudgets = budgetRepository.getAnnualBudgets(owner, year);
        Map<CategoryId, Budget> defaultBudgets = budgetRepository.getDefaultBudget(owner);

        return categoryService.getAllCategories(owner)
                .filter(categoryPredicate)
                .map(CategoryDTO::getId)
                .collect(Collectors.toMap(
                        categoryId -> categoryId,
                        categoryId -> Optional.ofNullable(monthlyBudgets.get(categoryId))
                                .map(monthMap -> monthMap.values().stream()
                                        .map(Budget::getAmount)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                                .orElseGet(() -> Optional.ofNullable(annualBudgets.get(categoryId))
                                        .map(Budget::getAmount)
                                        .orElseGet(() -> Optional.ofNullable(defaultBudgets.get(categoryId))
                                                .map(Budget::getAmount)
                                                .orElse(BigDecimal.ZERO)))
                ));
    }

    public List<BudgetDTO> getAllBudgets(String userOwner, int year) {
        Map<CategoryId, Map<YearMonth, Budget>> monthlyBudgets = budgetRepository.getMonthlyBudgets(userOwner, year);
        Map<CategoryId, Budget> defaultBudgets = budgetRepository.getDefaultBudget(userOwner);
        return categoryService.getAllCategories(userOwner)
                .map(CategoryDTO::getId)
                .filter(id -> monthlyBudgets.containsKey(id) || defaultBudgets.containsKey(id))
                .flatMap(categoryId -> Optional.ofNullable(monthlyBudgets.get(categoryId))
                        .map(yearMonthMonthlyBudgetMap ->
                                yearMonthMonthlyBudgetMap
                                        .values()
                                        .stream()
                                        .map(BudgetDTO::from))
                        .orElseGet(() -> Stream.of(defaultBudgets.get(categoryId))
                                .map(BudgetDTO::from)))
                .filter(Objects::nonNull)
                .toList();
    }

    public List<BudgetDTO> getAllBudgets(String userOwner, int year, CategoryId categoryId) {
        Map<CategoryId, Map<YearMonth, Budget>> monthlyBudgets = budgetRepository.getMonthlyBudgets(userOwner, year);
        Map<CategoryId, Budget> defaultBudgets = budgetRepository.getAnnualBudgets(userOwner, year);
        return getUserCategoryById(userOwner, categoryId)
                .map(CategoryDTO::getId)
                .map(catId -> getYearMonths(year)
                        .map(yearMonth -> getMonthlyBudget(catId, yearMonth, monthlyBudgets)
                                .orElseGet(() -> getDefaultBudget(catId, defaultBudgets)
                                        .orElse(null)))
                        .toList())
                .orElseThrow(() -> new IllegalArgumentException("Category not found or does not belong to user"));
    }

    private Optional<CategoryDTO> getUserCategoryById(String userOwner, CategoryId categoryId) {
        return Optional.ofNullable(categoryService.getCategory(categoryId))
                .filter(category -> category.getUserOwner().equals(userOwner));
    }


    public List<BudgetDTO> getAllBudgets(String userOwner, YearMonth yearMonth) {
        Map<CategoryId, Map<YearMonth, Budget>> monthlyBudgets = budgetRepository.getMonthlyBudgets(userOwner, yearMonth.getYear());
        Map<CategoryId, Budget> defaultBudgets = budgetRepository.getAnnualBudgets(userOwner, yearMonth.getYear());
        return categoryService.getAllCategories(userOwner)
                .map(CategoryDTO::getId)
                .map(categoryId -> getMonthlyBudget(categoryId, yearMonth, monthlyBudgets)
                        .orElseGet(() -> getDefaultBudget(categoryId, defaultBudgets)
                                .orElse(null)))
                .toList();
    }

    public BudgetDTO getMonthlyBudgetByCategory(String userOwner, YearMonth yearMonth, CategoryId categoryId) {
        Map<CategoryId, Map<YearMonth, Budget>> monthlyBudgets = budgetRepository.getMonthlyBudgets(userOwner, yearMonth.getYear());
        Map<CategoryId, Budget> defaultBudgets = budgetRepository.getAnnualBudgets(userOwner, yearMonth.getYear());
        return getUserCategoryById(userOwner, categoryId)
                .map(CategoryDTO::getId)
                .map(catId -> getMonthlyBudget(catId, yearMonth, monthlyBudgets)
                        .orElseGet(() -> getDefaultBudget(catId, defaultBudgets)
                                .orElse(null)))
                .orElseThrow(() -> new IllegalArgumentException("Category not found or does not belong to user"));
    }


    private static Optional<BudgetDTO> getDefaultBudget(CategoryId categoryId, Map<CategoryId, Budget> defaultBudgets) {
        return Optional.ofNullable(defaultBudgets.get(categoryId))
                .map(BudgetDTO::from);
    }

    private static Optional<BudgetDTO> getMonthlyBudget(CategoryId categoryId, YearMonth yearMonth, Map<CategoryId, Map<YearMonth, Budget>> monthlyBudgets) {
        return Optional.ofNullable(monthlyBudgets.get(categoryId))
                .map(monthMap -> monthMap.get(yearMonth))
                .map(BudgetDTO::from);
    }

    private static Stream<YearMonth> getYearMonths(int year) {
        return IntStream.range(0, 12)
                .mapToObj(month -> YearMonth.of(year, month + 1));
    }

    public BigDecimal getTotalIncome(String username, int year, Integer month) {

        return getCategoryIdBigDecimalMap(username, year, month, category -> category.getMacroCategory().isIncome())
                .values()
                .stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalExpense(String username, int year, Integer month) {

        return getCategoryIdBigDecimalMap(username, year, month, category -> category.getMacroCategory().isExpense())
                .values()
                .stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalSavings(String username, int year, Integer month) {

        return getCategoryIdBigDecimalMap(username, year, month, category -> category.getMacroCategory().isSavings())
                .values()
                .stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
