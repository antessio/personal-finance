package antessio.personalfinance.domain.ports;

import antessio.personalfinance.domain.model.Budget;
import antessio.personalfinance.domain.model.BudgetId;
import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.model.MonthlyBudget;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public interface BudgetRepository {

    Map<CategoryId, Budget> getDefaultBudgets(String userOwner, int year);
    Map<CategoryId, Budget> getDefaultBudgets(String userOwner);

    Map<CategoryId, Map<YearMonth, MonthlyBudget>> getMonthlyBudgets(String userOwner, int year);

    void create(MonthlyBudget monthlyBudget);

    void create(Budget budget);

    List<Budget> getByIds(List<BudgetId> createdIds);
}
