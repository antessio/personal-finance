package antessio.personalfinance.domain.events;

import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.model.TransactionId;

public record TransactionCategoryAssigned(TransactionId id, CategoryId categoryId) implements TransactionEvent {
    @Override
    public TransactionId getTransactionId() {
        return id;
    }
}
