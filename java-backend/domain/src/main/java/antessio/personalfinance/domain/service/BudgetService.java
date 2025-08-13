package antessio.personalfinance.domain.service;

import antessio.personalfinance.domain.dto.BudgetDTO;
import antessio.personalfinance.domain.dto.CategoryDTO;
import antessio.personalfinance.domain.dto.CreateDefaultBudgetDTO;
import antessio.personalfinance.domain.dto.CreateMonthlyBudgetDTO;
import antessio.personalfinance.domain.model.Budget;
import antessio.personalfinance.domain.model.BudgetId;
import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.model.MonthlyBudget;
import antessio.personalfinance.domain.ports.BudgetRepository;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryService categoryService;

    public BudgetService(BudgetRepository budgetRepository, CategoryService categoryService) {
        this.budgetRepository = budgetRepository;
        this.categoryService = categoryService;
    }

    public void createMonthlyBudget(CreateMonthlyBudgetDTO createMonthlyBudgetDTO) {
        CategoryDTO category = getUserCategoryById(createMonthlyBudgetDTO.getUserOwner(), createMonthlyBudgetDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found or does not belong to user"));
        budgetRepository.create(new MonthlyBudget(BudgetId.generate(), category.getId(), createMonthlyBudgetDTO.getAmount(), createMonthlyBudgetDTO.getYearMonth()));
    }
    public void createDefaultBudget(CreateDefaultBudgetDTO createDefaultBudgetDTO) {
        CategoryDTO category = getUserCategoryById(createDefaultBudgetDTO.getUserOwner(), createDefaultBudgetDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found or does not belong to user"));
        budgetRepository.create(new Budget(BudgetId.generate(), category.getId(), createDefaultBudgetDTO.getAmount()));
    }

    public void createDefaultBudgets(Set<CreateDefaultBudgetDTO> createDefaultBudgetDTOs) {
        for (CreateDefaultBudgetDTO createDefaultBudgetDTO : createDefaultBudgetDTOs) {
            createDefaultBudget(createDefaultBudgetDTO);
        }
    }

    public List<BudgetDTO> getAllBudgets(String userOwner, int year) {
        Map<CategoryId, Map<YearMonth, MonthlyBudget>> monthlyBudgets = budgetRepository.getMonthlyBudgets();
        Map<CategoryId, Budget> defaultBudgets = budgetRepository.getDefaultBudgets();
        return categoryService.getAllCategories(userOwner)
                .map(CategoryDTO::getId)
                .flatMap(categoryId -> getYearMonths(year)
                        .map(yearMonth -> getMonthlyBudget(categoryId, yearMonth, monthlyBudgets)
                                .orElseGet(() -> getDefaultBudget(categoryId, defaultBudgets)
                                        .orElseGet(() -> new BudgetDTO(categoryId, BigDecimal.ZERO)))))
                .toList();
    }

    public List<BudgetDTO> getAllBudgets(String userOwner, int year, CategoryId categoryId) {
        Map<CategoryId, Map<YearMonth, MonthlyBudget>> monthlyBudgets = budgetRepository.getMonthlyBudgets();
        Map<CategoryId, Budget> defaultBudgets = budgetRepository.getDefaultBudgets();
        return getUserCategoryById(userOwner, categoryId)
                .map(CategoryDTO::getId)
                .map(catId -> getYearMonths(year)
                        .map(yearMonth -> getMonthlyBudget(catId, yearMonth, monthlyBudgets)
                                .orElseGet(() -> getDefaultBudget(catId, defaultBudgets)
                                        .orElseGet(() -> new BudgetDTO(catId, BigDecimal.ZERO))))
                        .toList())
                .orElseThrow(() -> new IllegalArgumentException("Category not found or does not belong to user"));
    }

    private Optional<CategoryDTO> getUserCategoryById(String userOwner, CategoryId categoryId) {
        return Optional.ofNullable(categoryService.getCategory(categoryId))
                .filter(category -> category.getUserOwner().equals(userOwner));
    }


    public List<BudgetDTO> getAllBudgets(String userOwner, YearMonth yearMonth) {
        Map<CategoryId, Map<YearMonth, MonthlyBudget>> monthlyBudgets = budgetRepository.getMonthlyBudgets();
        Map<CategoryId, Budget> defaultBudgets = budgetRepository.getDefaultBudgets();
        return categoryService.getAllCategories(userOwner)
                .map(CategoryDTO::getId)
                .map(categoryId -> getMonthlyBudget(categoryId, yearMonth, monthlyBudgets)
                        .orElseGet(() -> getDefaultBudget(categoryId, defaultBudgets)
                                .orElseGet(() -> new BudgetDTO(categoryId, BigDecimal.ZERO))))
                .toList();
    }

    public BudgetDTO getMonthlyBudgetByCategory(String userOwner, YearMonth yearMonth, CategoryId categoryId) {
        Map<CategoryId, Map<YearMonth, MonthlyBudget>> monthlyBudgets = budgetRepository.getMonthlyBudgets();
        Map<CategoryId, Budget> defaultBudgets = budgetRepository.getDefaultBudgets();
        return getUserCategoryById(userOwner, categoryId)
                .map(CategoryDTO::getId)
                .map(catId -> getMonthlyBudget(catId, yearMonth, monthlyBudgets)
                        .orElseGet(() -> getDefaultBudget(catId, defaultBudgets)
                                .orElseGet(() -> new BudgetDTO(catId, BigDecimal.ZERO))))
                .orElseThrow(() -> new IllegalArgumentException("Category not found or does not belong to user"));
    }


    private static Optional<BudgetDTO> getDefaultBudget(CategoryId categoryId, Map<CategoryId, Budget> defaultBudgets) {
        return Optional.ofNullable(defaultBudgets.get(categoryId))
                .map(defaultBudget -> new BudgetDTO(defaultBudget.getCategoryId(), defaultBudget.getAmount()));
    }

    private static Optional<BudgetDTO> getMonthlyBudget(CategoryId categoryId, YearMonth yearMonth, Map<CategoryId, Map<YearMonth, MonthlyBudget>> monthlyBudgets) {
        return Optional.ofNullable(monthlyBudgets.get(categoryId))
                .map(monthMap -> monthMap.get(yearMonth))
                .map(monthlyBudget -> new BudgetDTO(monthlyBudget.getCategoryId(), monthlyBudget.getAmount()));
    }

    private static Stream<YearMonth> getYearMonths(int year) {
        return IntStream.range(0, 12)
                .mapToObj(month -> YearMonth.of(year, month + 1));
    }
}
