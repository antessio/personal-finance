package antessio.personalfinance.infrastructure.persistence.repository;

import antessio.personalfinance.domain.model.Category;
import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.model.CategoryMatcher;
import antessio.personalfinance.domain.ports.CategoryRepository;
import antessio.personalfinance.infrastructure.persistence.entity.CategoryEntity;
import antessio.personalfinance.infrastructure.persistence.entity.CategoryMatcherEntity;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        CategoryEntity categoryEntity = new CategoryEntity(
                null,
                category.getName(),
                category.getMacroCategory(),
                category.getType(),
                category.getEmoji(),
                category.getUserOwner(),
                null,
                category.getInsertedAt(),
                category.getUpdatedAt()
        );
        category.getMatchers().stream()
                .map(cm -> new CategoryMatcherEntity(null, cm.getMatcher(), null, categoryEntity))
                .forEach(categoryEntity::addMatcher);


        CategoryEntity categoryEntityStored = categorySpringDataRepository.save(categoryEntity);
        return toDomain(categoryEntityStored);
    }

    @Override
    public void delete(Category category) {
        categorySpringDataRepository.delete(new CategoryEntity(
                category.getId().id(),
                category.getName(),
                category.getMacroCategory(),
                category.getType(),
                category.getEmoji(),
                category.getUserOwner(),
                null,
                category.getInsertedAt(),
                category.getUpdatedAt()
        ));
    }

    @Override
    public void update(Category category) {
        // 1. Fetch the existing entity from the database
        CategoryEntity existingEntity = categorySpringDataRepository.findById(category.getId().id())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + category.getId().id()));
        existingEntity.setName(category.getName());
        existingEntity.setMacroCategory(category.getMacroCategory());
        existingEntity.setType(category.getType());
        existingEntity.setEmoji(category.getEmoji());
        existingEntity.setUserOwner(category.getUserOwner());
        existingEntity.setInsertedAt(category.getInsertedAt());
        existingEntity.setUpdatedAt(category.getUpdatedAt());
        syncMatchers(existingEntity, category.getMatchers());
        categorySpringDataRepository.save(existingEntity);

    }

    // Extract the collection merge logic to keep the update method clean
    private void syncMatchers(CategoryEntity existingEntity, Set<CategoryMatcher> incomingMatchers) {
        Set<CategoryMatcherEntity> existingMatchers = existingEntity.getMatchers();

        // If domain says there are no matchers, clear the DB matchers
        if (incomingMatchers == null || incomingMatchers.isEmpty()) {
            existingMatchers.clear(); // Hibernate will issue DELETE statements for all orphans
            return;
        }

        Set<Pair<String, Integer>> incomingKeys = incomingMatchers
                .stream()
                .map(cm -> Pair.of(cm.getMatcher(), cm.getYear()))
                .collect(Collectors.toSet());


        // A. REMOVE: Delete matchers that exist in the DB but are missing from the Domain object
        existingMatchers.removeIf(dbMatcher -> !incomingKeys.contains(Pair.of(dbMatcher.getMatcher(), dbMatcher.getYear())));

        // B. ADD or UPDATE
        for (CategoryMatcher incoming : incomingMatchers) {
            // Look for the matcher in the currently attached DB entity
            existingMatchers.stream()
                    .filter(dbMatcher -> dbMatcher.getMatcher().equals(incoming.getMatcher())
                                         && Objects.equals(dbMatcher.getYear(), incoming.getYear()))
                    .findFirst()
                    .ifPresentOrElse(dbMatcher -> {
                                dbMatcher.setYear(incoming.getYear());
                                dbMatcher.setMatcher(incoming.getMatcher());

                            }, () -> {
                                CategoryMatcherEntity newMatcher = new CategoryMatcherEntity(null, incoming.getMatcher(), incoming.getYear(), existingEntity);
                                existingEntity.addMatcher(newMatcher);
                            }
                    );
        }
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

    @Override
    public List<Category> findByIdsAndUser(List<CategoryId> categoryIds, String userOwner) {
        return categorySpringDataRepository.findAllById(categoryIds.stream().map(CategoryId::id).toList())
                .stream()
                .filter(c -> c.getUserOwner().equals(userOwner))
                .map(CategoryRepositoryAdapter::toDomain)
                .toList();
    }

    private static Category toDomain(CategoryEntity entity) {
        return new Category(
                new CategoryId(entity.getId()),
                entity.getName(),
                entity.getMacroCategory(),
                entity.getType(),
                entity.getEmoji(),
                entity.getUserOwner(),
                entity.getMatchers()
                        .stream()
                        .map(cm -> CategoryMatcher.of(cm.getMatcher(), cm.getYear()))
                        .collect(Collectors.toSet()),
                entity.getInsertedAt(),
                entity.getUpdatedAt()
        );
    }

}
