package antessio.personalfinance.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import antessio.personalfinance.domain.model.TransactionImport;
import antessio.personalfinance.domain.model.TransactionImportId;
import antessio.personalfinance.domain.ports.TransactionImportRepository;
import antessio.personalfinance.infrastructure.persistence.entity.TransactionImportEntity;

@Component
public class TransactionImportRepositoryAdapter implements TransactionImportRepository {

    private final TransactionImportSpringDataRepository transactionImportSpringDataRepository;

    public TransactionImportRepositoryAdapter(TransactionImportSpringDataRepository transactionImportSpringDataRepository) {
        this.transactionImportSpringDataRepository = transactionImportSpringDataRepository;
    }

    @Override
    public Optional<TransactionImport> findById(TransactionImportId id) {
        return transactionImportSpringDataRepository.findById(id.id())
                                                    .map(TransactionImportRepositoryAdapter::toDomain);
    }



    @Override
    public TransactionImport save(TransactionImport TransactionImport) {
        TransactionImportEntity transactionImportEntity = transactionImportSpringDataRepository.save(toEntity(null, TransactionImport));
        return toDomain(transactionImportEntity);
    }

    @Override
    public void delete(TransactionImport transactionImport) {
        transactionImportSpringDataRepository.delete(toEntity(transactionImport.getId().id(), transactionImport));
    }



    @Override
    public void update(TransactionImport transactionImport) {
        transactionImportSpringDataRepository.save(toEntity(transactionImport.getId().id(), transactionImport));
    }

    @Override
    public List<TransactionImport> findAllByUser(String userId, int limit, TransactionImportId startingAfterId) {
        return Optional.ofNullable(startingAfterId)
                .map(TransactionImportId::id)
                .map(cursor -> transactionImportSpringDataRepository.findByUserOwnerAndIdGreaterThanAndLimit(userId, cursor, limit))
                .orElseGet(()-> transactionImportSpringDataRepository.findByUserOwnerAndLimit(userId, limit))
                .stream()
                .map(TransactionImportRepositoryAdapter::toDomain)
                .toList();
    }

    private static TransactionImport toDomain(TransactionImportEntity transactionImportEntity) {
        return new TransactionImport(
                new TransactionImportId(transactionImportEntity.getId()),
                transactionImportEntity.getSourceType(),
                transactionImportEntity.getFilePath(),
                transactionImportEntity.getStatus(),
                transactionImportEntity.getUserOwner(),
                transactionImportEntity.getInsertedAt(),
                transactionImportEntity.getUpdatedAt()
        );
    }
    private static TransactionImportEntity toEntity(Long id, TransactionImport transactionImport) {
        return new TransactionImportEntity(
                id,
                transactionImport.getSourceType(),
                transactionImport.getFilePath(),
                transactionImport.getStatus(),
                transactionImport.getUserOwner(),
                transactionImport.getInsertedAt(),
                transactionImport.getUpdatedAt()
        );
    }
}
