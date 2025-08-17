package antessio.personalfinance.domain.ports;

import antessio.personalfinance.domain.model.Budget;
import antessio.personalfinance.domain.model.BudgetId;
import antessio.personalfinance.domain.model.CategoryId;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public interface BudgetRepository {

    Map<CategoryId, Budget> getDefaultBudget(String userOwner);
    Map<CategoryId, Budget> getAnnualBudgets(String userOwner, int year);
    Map<CategoryId, Map<YearMonth, Budget>> getMonthlyBudgets(String userOwner, int year);

    void create(Budget budget);

    List<Budget> getByIds(List<BudgetId> createdIds);
}
