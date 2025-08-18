package antessio.personalfinance.domain.model;

import antessio.personalfinance.common.Id;
import com.github.f4b6a3.uuid.UuidCreator;
import java.util.UUID;

public class BudgetId extends Id<UUID> {
    public BudgetId(UUID id) {
        super(id);
    }

    public static BudgetId generate() {
        return new BudgetId(UuidCreator.getTimeOrderedEpoch());
    }

    public static BudgetId fromString(String id) {
        return new BudgetId(UUID.fromString(id));
    }

}
