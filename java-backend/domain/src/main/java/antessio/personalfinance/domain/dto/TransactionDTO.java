package antessio.personalfinance.domain.dto;

import antessio.personalfinance.domain.model.TransactionId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionDTO(TransactionId id, LocalDate date, BigDecimal amount, String description, String source,
                             Boolean skip, String userOwner, CategoryDTO category, LocalDateTime insertedAt,
                             LocalDateTime updatedAt) {
}
