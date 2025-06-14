package antessio.personalfinance.domain.service;

import antessio.personalfinance.domain.dto.CategoryDTO;
import antessio.personalfinance.domain.dto.CreateCategoryDTO;
import antessio.personalfinance.domain.model.Category;
import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.model.MacroCategoryEnum;
import antessio.personalfinance.domain.ports.CategoryRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryDTO createCategory(CreateCategoryDTO createCategoryDTO) {
        return createCategory(createCategoryDTO.userId(), createCategoryDTO.name(), createCategoryDTO.macroCategory(), createCategoryDTO.emoji());
    }

    public CategoryDTO createCategory(String userId, String name, MacroCategoryEnum macroCategory, String emoji) {
        Category category = new Category(null, name, macroCategory, emoji, userId, null, Instant.now(), null);
        Category savedCategory = categoryRepository.save(category);
        return toDTO(savedCategory);
    }

    public void updateCategoryMatchers(CategoryId categoryId, Set<String> matchers) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        category.updateMatchers(matchers);
        categoryRepository.update(category);
    }

    public void deleteCategory(CategoryId categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        categoryRepository.delete(category);
    }

    public CategoryDTO getCategory(CategoryId categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        return toDTO(category);
    }

    public List<CategoryDTO> getAllCategories(String userId, int limit, CategoryId cursor) {
        return categoryRepository.findAllByUser(userId, limit, cursor)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public Optional<CategoryId> findMatchingCategoryId(String userId, String description) {
        return categoryRepository.findAllByUser(userId, 500)
                .stream()
                .filter(c -> c.matches(description))
                .map(Category::getId)
                .findFirst();
    }


    public List<CategoryDTO> getCategoriesByIdsAndUser(List<CategoryId> categoryIds, String userOwner) {
        return categoryRepository.findByIdsAndUser(categoryIds, userOwner)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private CategoryDTO toDTO(Category category) {
        return new CategoryDTO(
                category.getId(),
                category.getName(),
                category.getMacroCategory(),
                category.getEmoji(),
                category.getUserOwner(),
                category.getMatchers(),
                category.getInsertedAt(),
                category.getUpdatedAt()
        );
    }
}
