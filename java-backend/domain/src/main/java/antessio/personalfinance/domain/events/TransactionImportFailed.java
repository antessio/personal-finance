package antessio.personalfinance.domain.events;

import antessio.personalfinance.domain.model.TransactionImportId;

public record TransactionImportFailed(TransactionImportId id) {
}
