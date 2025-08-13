package antessio.personalfinance.domain.events;

import antessio.personalfinance.domain.model.TransactionId;

public interface TransactionEvent {
    TransactionId getTransactionId();
}
