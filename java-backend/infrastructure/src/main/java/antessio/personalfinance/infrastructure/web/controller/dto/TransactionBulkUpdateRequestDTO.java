package antessio.personalfinance.infrastructure.web.controller.dto;

public record TransactionBulkUpdateRequestDTO(String[] transactionIds,
                                              Long categoryId,
                                              Boolean skip) {
}
