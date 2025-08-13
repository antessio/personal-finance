package antessio.personalfinance.domain.dto;

import java.time.LocalDateTime;

import antessio.personalfinance.domain.model.TransactionImportId;
import antessio.personalfinance.domain.model.TransactionUploadStatus;

public record TransactionImportDTO(TransactionImportId id,
                                   String sourceType,
                                   String filePath,
                                   TransactionUploadStatus status,
                                   String userOwner,
                                   LocalDateTime insertedAt,
                                   LocalDateTime updatedAt) {

}
