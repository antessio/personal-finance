package antessio.personalfinance.infrastructure.persistence.repository;

import antessio.personalfinance.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {
    List<TransactionEntity> findByUserOwner(String userOwner);
    List<TransactionEntity> findByUserOwnerAndDateBetween(String userOwner, LocalDate startDate, LocalDate endDate);
    List<TransactionEntity> findByUserOwnerAndCategoryId(String userOwner, String categoryId);
} 