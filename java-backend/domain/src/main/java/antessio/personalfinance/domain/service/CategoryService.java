package antessio.personalfinance.domain.service;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import antessio.personalfinance.domain.dto.CategoryDTO;
import antessio.personalfinance.domain.model.Category;
import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.model.MacroCategoryEnum;
import antessio.personalfinance.domain.ports.CategoryRepository;

public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }

    public CategoryDTO createCategory(String userId, String name, MacroCategoryEnum macroCategory) {
        Category category = new Category(null, name, macroCategory, userId, null, Instant.now(), null);
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
    public List<CategoryDTO> getAllCategories(String userId, CategoryId cursor, int limit) {
        return categoryRepository.findAllByUser(userId, limit, cursor)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private CategoryDTO toDTO(Category category) {
        return new CategoryDTO(
            category.getId(),
            category.getName(),
            category.getMacroCategory(),
            category.getUserOwner(),
            category.getMatchers(),
            category.getInsertedAt(),
            category.getUpdatedAt()
        );
    }

}
