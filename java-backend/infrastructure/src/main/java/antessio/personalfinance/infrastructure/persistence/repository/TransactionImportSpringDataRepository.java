package antessio.personalfinance.infrastructure.persistence.repository;

import antessio.personalfinance.infrastructure.persistence.entity.TransactionImportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionImportSpringDataRepository extends JpaRepository<TransactionImportEntity, Long> {

  @Query(value = """
      SELECT *
        FROM transaction_import_entity t
       WHERE t.user_owner = :userOwner
      """,
      nativeQuery = true)
  List<TransactionImportEntity> findByUserOwner(
      @Param("userOwner") String userOwner
  );

  @Query(value = """
      SELECT *
        FROM transaction_import_entity t
       WHERE t.user_owner = :userOwner
       ORDER BY t.id
       LIMIT :limit
      """,
      nativeQuery = true)
  List<TransactionImportEntity> findByUserOwnerWithLimit(
      @Param("userOwner") String userOwner,
      @Param("limit") int limit
  );

  @Query(value = """
      SELECT *
        FROM transaction_import_entity t
       WHERE t.user_owner      = :userOwner
         AND t.id              > :startingAfterId
       ORDER BY t.id
       LIMIT :limit
      """,
      nativeQuery = true)
  List<TransactionImportEntity> findByUserOwnerAfterIdWithLimit(
      @Param("userOwner") String userOwner,
      @Param("startingAfterId") Long startingAfterId,
      @Param("limit") int limit
  );

} 