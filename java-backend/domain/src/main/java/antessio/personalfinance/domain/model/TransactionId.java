package antessio.personalfinance.domain.model;

import java.util.UUID;

public record TransactionId(UUID id) {
    public static TransactionId generate() {
        return new TransactionId(UUID.randomUUID());
    }

    public static TransactionId fromString(String id) {
        return new TransactionId(UUID.fromString(id));
    }

}
