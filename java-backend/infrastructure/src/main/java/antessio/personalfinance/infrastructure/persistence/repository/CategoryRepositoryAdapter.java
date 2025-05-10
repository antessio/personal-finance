package antessio.personalfinance.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import antessio.personalfinance.domain.model.Category;
import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.ports.CategoryRepository;
import antessio.personalfinance.infrastructure.persistence.entity.CategoryEntity;

@Component
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategorySpringDataRepository categorySpringDataRepository;

    public CategoryRepositoryAdapter(CategorySpringDataRepository categorySpringDataRepository) {
        this.categorySpringDataRepository = categorySpringDataRepository;
    }


    @Override
    public Optional<Category> findById(CategoryId id) {
        return categorySpringDataRepository.findById(id.id())
                                           .map(CategoryRepositoryAdapter::toDomain);
    }

    @Override
    public Category save(Category category) {
        CategoryEntity categoryEntity = categorySpringDataRepository.save(new CategoryEntity(
                null,
                category.getName(),
                category.getMacroCategory(),
                category.getUserOwner(),
                category.getMatchers(),
                category.getInsertedAt(),
                category.getUpdatedAt()
        ));
        return toDomain(categoryEntity);
    }

    @Override
    public void delete(Category category) {
        categorySpringDataRepository.delete(new CategoryEntity(
                category.getId().id(),
                category.getName(),
                category.getMacroCategory(),
                category.getUserOwner(),
                category.getMatchers(),
                category.getInsertedAt(),
                category.getUpdatedAt()
        ));
    }

    @Override
    public void update(Category category) {
        categorySpringDataRepository.save(new CategoryEntity(
                category.getId().id(),
                category.getName(),
                category.getMacroCategory(),
                category.getUserOwner(),
                category.getMatchers(),
                category.getInsertedAt(),
                category.getUpdatedAt()
        ));
    }

    @Override
    public List<Category> findAllByUser(String userId, int limit, CategoryId startingAfterId) {
        return Optional.ofNullable(startingAfterId)
                       .map(CategoryId::id)
                       .map(id -> categorySpringDataRepository.findAllByUser(userId, limit, id))
                       .orElseGet(() -> categorySpringDataRepository.findAllByUser(userId, limit))
                       .stream()
                       .map(CategoryRepositoryAdapter::toDomain)
                       .toList();

    }

    @Override
    public List<Category> findAllByUser(String userId, int limit) {
        return categorySpringDataRepository.findAllByUser(userId, limit)
                                           .stream()
                                           .map(CategoryRepositoryAdapter::toDomain)
                                           .toList();
    }

    private static Category toDomain(CategoryEntity entity) {
        return new Category(
                new CategoryId(entity.getId()),
                entity.getName(),
                entity.getMacroCategory(),
                entity.getUserOwner(),
                entity.getMatchers(),
                entity.getInsertedAt(),
                entity.getUpdatedAt()
        );
    }

}
