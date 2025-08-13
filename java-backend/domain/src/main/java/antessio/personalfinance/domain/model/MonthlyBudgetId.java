package antessio.personalfinance.domain.model;

import antessio.personalfinance.common.Id;

import java.util.UUID;

public class MonthlyBudgetId extends Id<UUID> {
    public MonthlyBudgetId(UUID id) {
        super(id);
    }

    public static MonthlyBudgetId generate() {
        return new MonthlyBudgetId(UUID.randomUUID());
    }

    public static MonthlyBudgetId fromString(String id) {
        return new MonthlyBudgetId(UUID.fromString(id));
    }


}
