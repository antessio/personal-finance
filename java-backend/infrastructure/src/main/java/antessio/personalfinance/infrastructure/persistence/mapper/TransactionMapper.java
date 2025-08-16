package antessio.personalfinance.infrastructure.persistence.mapper;

import antessio.personalfinance.domain.model.Transaction;
import antessio.personalfinance.domain.model.TransactionImportId;
import antessio.personalfinance.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toDomain(TransactionEntity entity) {
        return new Transaction(
                entity.getTransactionId(),
                entity.getDate(),
                entity.getAmount(),
                entity.getDescription(),
                entity.getUniqueId(),
                entity.getSource(),
                entity.getSkip(),
                entity.getUserOwner(),
                entity.getCategoryId(),
                entity.getInsertedAt(),
                entity.getUpdatedAt(),
                new TransactionImportId(entity.getTransactionImportId())
        );
    }

    public TransactionEntity toEntity(Transaction domain) {
        return new TransactionEntity(
                domain.getId().id().toString(),
                domain.getDate(),
                domain.getAmount(),
                domain.getDescription(),
                domain.getUniqueId(),
                domain.getSource(),
                domain.getSkip(),
                domain.getUserOwner(),
                domain.getCategoryId() != null ? domain.getCategoryId().id() : null,
                domain.getInsertedAt(),
                domain.getUpdatedAt(),
                domain.getTransactionImportId().id()
        );
    }
}