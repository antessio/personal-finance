package antessio.personalfinance.domain.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TransactionImport {

    private TransactionImportId id;

    private String sourceType;

    private String filePath;

    private TransactionUploadStatus status;

    private String userOwner;

    private LocalDateTime insertedAt;

    private LocalDateTime updatedAt;

    public void success() {
        this.status = TransactionUploadStatus.SUCCESS;
    }

    public void fail() {
        this.status = TransactionUploadStatus.FAILED;
    }

}
