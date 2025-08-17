package antessio.personalfinance.infrastructure.persistence.repository;

import antessio.personalfinance.domain.model.Budget;
import antessio.personalfinance.domain.model.BudgetId;
import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.model.MonthlyBudget;
import antessio.personalfinance.domain.ports.BudgetRepository;
import antessio.personalfinance.infrastructure.persistence.entity.BudgetEntity;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BudgetRepositoryAdapter implements BudgetRepository {

    private final BudgetSpringDataRepository budgetSpringDataRepository;

    public BudgetRepositoryAdapter(BudgetSpringDataRepository budgetSpringDataRepository) {
        this.budgetSpringDataRepository = budgetSpringDataRepository;
    }


    @Override
    public Map<CategoryId, Budget> getDefaultBudgets(String userOwner) {
        return this.budgetSpringDataRepository.findAllByUser(userOwner, Integer.MAX_VALUE)
                .stream()
                .collect(Collectors.toMap(
                        BudgetEntity::getCategoryIdObj,
                        budgetEntity -> new Budget(
                                budgetEntity.getBudgetId(),
                                budgetEntity.getCategoryIdObj(),
                                budgetEntity.getAmount(),
                                budgetEntity.getUserOwner(),
                                budgetEntity.getYear(),
                                budgetEntity.getMonth()
                        ),
                        (existing, replacement) -> existing // In case of duplicates, keep the existing one
                ));
    }

    @Override
    public Map<CategoryId, Budget> getDefaultBudgets(String userOwner, int year) {
        return this.budgetSpringDataRepository.findAllByUserAndYearAndMonthNull(userOwner, year, Integer.MAX_VALUE)
                .stream()
                .collect(Collectors.toMap(
                        BudgetEntity::getCategoryIdObj,
                        budgetEntity -> new Budget(
                                budgetEntity.getBudgetId(),
                                budgetEntity.getCategoryIdObj(),
                                budgetEntity.getAmount(),
                                budgetEntity.getUserOwner(),
                                budgetEntity.getYear(),
                                budgetEntity.getMonth()
                        ),
                        (existing, replacement) -> existing // In case of duplicates, keep the existing one
                ));
    }

    @Override
    public Map<CategoryId, Map<YearMonth, MonthlyBudget>> getMonthlyBudgets(String userOwner, int year) {
        return budgetSpringDataRepository.findAllByUserAndYear(userOwner, year, Integer.MAX_VALUE)
                .stream()
                .collect(Collectors.groupingBy(
                        BudgetEntity::getCategoryIdObj,
                        Collectors.toMap(
                                budgetEntity -> YearMonth.of(budgetEntity.getYear(), budgetEntity.getMonth()),
                                budgetEntity -> new MonthlyBudget(
                                        budgetEntity.getBudgetId(),
                                        budgetEntity.getCategoryIdObj(),
                                        budgetEntity.getAmount(),
                                        YearMonth.of(budgetEntity.getYear(), budgetEntity.getMonth()),
                                        budgetEntity.getUserOwner()
                                )
                        )));
    }

    @Override
    public void create(MonthlyBudget monthlyBudget) {
        BudgetEntity budgetEntity = new BudgetEntity(
                monthlyBudget.getId().getId().toString(),
                monthlyBudget.getCategoryId().getId(),
                monthlyBudget.getAmount(),
                monthlyBudget.getUserOwner(),
                monthlyBudget.getYearMonth().getYear(),
                monthlyBudget.getYearMonth().getMonthValue());


        budgetSpringDataRepository.save(budgetEntity);
    }

    @Override
    public void create(Budget budget) {
        BudgetEntity budgetEntity = new BudgetEntity(
                budget.getId().getId().toString(),
                budget.getCategoryId().getId(),
                budget.getAmount(),
                budget.getUserOwner(),
                null,
                null);


        budgetSpringDataRepository.save(budgetEntity);
    }

    @Override
    public List<Budget> getByIds(List<BudgetId> createdIds) {
        return budgetSpringDataRepository.findAllById(
                        createdIds.stream()
                                .map(budgetId -> budgetId.getId().toString())
                                .collect(Collectors.toSet()))
                .stream()
                .map(budgetEntity -> new Budget(
                        BudgetId.fromString(budgetEntity.getId()),
                        new CategoryId(budgetEntity.getCategoryId()),
                        budgetEntity.getAmount(),
                        budgetEntity.getUserOwner(),
                        budgetEntity.getYear(),
                        budgetEntity.getMonth()))
                .collect(Collectors.toList());
    }
}

