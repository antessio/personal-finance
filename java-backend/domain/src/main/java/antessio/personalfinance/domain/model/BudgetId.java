package antessio.personalfinance.domain.model;

import antessio.personalfinance.common.Id;

import java.util.UUID;

public class BudgetId extends Id<UUID> {
    public BudgetId(UUID id) {
        super(id);
    }

    public static BudgetId generate() {
        return new BudgetId(UUID.randomUUID());
    }

    public static BudgetId fromString(String id) {
        return new BudgetId(UUID.fromString(id));
    }


}
