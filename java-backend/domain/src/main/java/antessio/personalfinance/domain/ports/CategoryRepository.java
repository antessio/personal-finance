package antessio.personalfinance.domain.ports;

import java.util.List;
import java.util.Optional;

import antessio.personalfinance.domain.model.Category;
import antessio.personalfinance.domain.model.CategoryId;

public interface CategoryRepository {
    Optional<Category> findById(CategoryId id);

    Category save(Category category);

    void delete(Category category);

    void update(Category category);

    List<Category> findAllByUser(String userId, int limit, CategoryId startingAfterId);

    List<Category> findAllByUser(String userId, int limit);

    List<Category> findByIdsAndUser(List<CategoryId> categoryIds, String userOwner);
}
