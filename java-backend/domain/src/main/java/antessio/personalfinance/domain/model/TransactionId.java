package antessio.personalfinance.domain.model;

import antessio.personalfinance.common.Id;

import java.util.UUID;

public class TransactionId extends Id<UUID> {
    public TransactionId(UUID id) {
        super(id);
    }

    public static TransactionId generate() {
        return new TransactionId(UUID.randomUUID());
    }

    public static TransactionId fromString(String id) {
        return new TransactionId(UUID.fromString(id));
    }

}
