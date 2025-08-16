package antessio.personalfinance.domain.dto;

import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.model.TransactionId;

import java.util.List;

public record TransactionBulkUpdateDTO(List<TransactionId> transactionIds,
                                       CategoryId categoryId,
                                       Boolean skip) {
}
