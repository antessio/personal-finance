package antessio.personalfinance.infrastructure.persistence.entity;

import antessio.personalfinance.domain.model.TransactionImportId;
import antessio.personalfinance.domain.model.TransactionUploadStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_imports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionImportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "source_type", nullable = false)
    private String sourceType;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionUploadStatus status;

    @Column(name = "user_owner", nullable = false)
    private String userOwner;

    @Column(name = "inserted_at", nullable = false)
    private LocalDateTime insertedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public TransactionImportId getTransactionImportId() {
        return new TransactionImportId(id);
    }
}
