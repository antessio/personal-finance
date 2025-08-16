package antessio.personalfinance.infrastructure.persistence.entity;

import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.model.TransactionId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "unique_id", nullable = false, unique = true)
    private String uniqueId;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "skip", nullable = false)
    private Boolean skip;

    @Column(name = "user_owner", nullable = false)
    private String userOwner;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "inserted_at", nullable = false)
    private LocalDateTime insertedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "transaction_import_id")
    private Long transactionImportId;

    public TransactionId getTransactionId() {
        return TransactionId.fromString(id);
    }

    public CategoryId getCategoryId() {
        return categoryId != null ? new CategoryId(categoryId) : null;
    }
} 