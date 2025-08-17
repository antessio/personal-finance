package antessio.personalfinance.infrastructure.persistence.repository;

import antessio.personalfinance.infrastructure.persistence.entity.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetSpringDataRepository extends JpaRepository<BudgetEntity, Long> {

    @Query(nativeQuery = true, value = "SELECT * FROM budgets WHERE user_owner = ?1 AND year ?2 AND id > ?4 ORDER BY id LIMIT ?3")
    List<BudgetEntity> findAllByUserAndYearAndMonth(String userId, int year, int limit, String cursor);

    @Query(nativeQuery = true, value = "SELECT * FROM budgets WHERE user_owner = ?1 AND year = ?2 ORDER BY id LIMIT ?3")
    List<BudgetEntity> findAllByUserAndYearAndMonth(String userId, int year, int limit);

}

