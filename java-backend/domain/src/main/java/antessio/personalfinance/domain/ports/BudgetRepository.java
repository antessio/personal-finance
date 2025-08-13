package antessio.personalfinance.domain.ports;

import antessio.personalfinance.domain.model.Budget;
import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.model.MonthlyBudget;

import java.time.YearMonth;
import java.util.Map;

public interface BudgetRepository {

    Map<CategoryId, Budget> getDefaultBudgets();

    Map<CategoryId, Map<YearMonth, MonthlyBudget>> getMonthlyBudgets();

    void create(MonthlyBudget monthlyBudget);

    void create(Budget budget);
}
