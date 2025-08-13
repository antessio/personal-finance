package antessio.personalfinance.infrastructure.persistence.mapper;

import antessio.personalfinance.domain.model.TransactionImport;
import antessio.personalfinance.domain.model.TransactionImportId;
import antessio.personalfinance.infrastructure.persistence.entity.TransactionImportEntity;
import org.springframework.stereotype.Component;

@Component
public class TransactionImportMapper {
    
    public TransactionImport toDomain(TransactionImportEntity entity) {
        return new TransactionImport(
            entity.getTransactionImportId(),
            entity.getSourceType(),
            entity.getFilePath(),
            entity.getStatus(),
            entity.getUserOwner(),
            entity.getInsertedAt(),
            entity.getUpdatedAt()
        );
    }

    public TransactionImportEntity toEntity(TransactionImport domain) {
        return new TransactionImportEntity(
            domain.getId().id(),
            domain.getSourceType(),
            domain.getFilePath(),
            domain.getStatus(),
            domain.getUserOwner(),
            domain.getInsertedAt(),
            domain.getUpdatedAt()
        );
    }
} 