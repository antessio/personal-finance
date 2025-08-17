package antessio.personalfinance.infrastructure.persistence.repository;

import antessio.personalfinance.infrastructure.persistence.entity.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetSpringDataRepository extends JpaRepository<BudgetEntity, String> {

    @Query(nativeQuery = true, value = "SELECT * FROM budgets WHERE user_owner = ?1 AND year ?2 and month is null AND id > ?4 ORDER BY id LIMIT ?3")
    List<BudgetEntity> findAllByUserAndYear(String userId, int year, int limit, String cursor);

    @Query(nativeQuery = true, value = "SELECT * FROM budgets WHERE user_owner = ?1 AND year = ?2 and month is null ORDER BY id LIMIT ?3")
    List<BudgetEntity> findAllByUserAndYearAndMonthNull(String userId, int year, int limit);

    @Query(nativeQuery = true, value = "SELECT * FROM budgets WHERE user_owner = ?1 AND year = ?2 and month is not null ORDER BY id LIMIT ?3")
    List<BudgetEntity> findAllByUserAndYear(String userId, int year, int limit);

    @Query(nativeQuery = true, value = "SELECT * FROM budgets WHERE user_owner = ?1 ORDER BY id LIMIT ?2")
    List<BudgetEntity> findAllByUser(String userId, int limit);

}

