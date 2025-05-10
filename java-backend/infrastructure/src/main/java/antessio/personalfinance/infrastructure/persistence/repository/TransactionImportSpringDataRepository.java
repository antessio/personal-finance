package antessio.personalfinance.infrastructure.persistence.repository;

import antessio.personalfinance.infrastructure.persistence.entity.TransactionImportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionImportSpringDataRepository extends JpaRepository<TransactionImportEntity, Long> {
    List<TransactionImportEntity> findByUserOwner(String userOwner);
    List<TransactionImportEntity> findByUserOwnerAndIdGreaterThanAndLimit(String userOwner, Long startingAfterId, int limit);
    List<TransactionImportEntity> findByUserOwnerAndLimit(String userOwner, int limit);

} 