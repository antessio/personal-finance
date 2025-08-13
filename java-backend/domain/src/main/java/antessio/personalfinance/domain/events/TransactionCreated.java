package antessio.personalfinance.domain.events;

import antessio.personalfinance.domain.model.TransactionId;

public record TransactionCreated(TransactionId id) implements TransactionEvent {
    @Override
    public TransactionId getTransactionId() {
        return id;
    }
}
