package antessio.personalfinance.infrastructure.persistence.repository;

import antessio.personalfinance.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public interface CategorySpringDataRepository extends JpaRepository<CategoryEntity, Long> {

    @Query(nativeQuery = true, value = "SELECT * FROM categories WHERE user_owner = ?1 AND id > ?3 ORDER BY id LIMIT ?2")
    List<CategoryEntity> findAllByUser(String userId, int limit, long cursor);

    @Query(nativeQuery = true, value = "SELECT * FROM categories WHERE user_owner = ?1 ORDER BY id LIMIT ?2")
    List<CategoryEntity> findAllByUser(String userId, int limit);

}