package antessio.personalfinance.domain.service;

import antessio.personalfinance.domain.dto.CategoryDTO;
import antessio.personalfinance.domain.dto.CreateCategoryDTO;
import antessio.personalfinance.domain.model.AutomaticSkip;
import antessio.personalfinance.domain.model.Category;
import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.model.MacroCategoryEnum;
import antessio.personalfinance.domain.ports.AutomaticSkipRepository;
import antessio.personalfinance.domain.ports.CategoryRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AutomaticSkipRepository automaticSkipRepository;

    public CategoryService(CategoryRepository categoryRepository, AutomaticSkipRepository automaticSkipRepository) {
        this.categoryRepository = categoryRepository;
        this.automaticSkipRepository = automaticSkipRepository;
    }

    public CategoryDTO createCategory(String userOwner, CreateCategoryDTO createCategoryDTO) {
        return createCategory(userOwner, createCategoryDTO.name(), createCategoryDTO.macroCategory(), createCategoryDTO.emoji());
    }

    public CategoryDTO createCategory(String userId, String name, MacroCategoryEnum macroCategory, String emoji) {
        Category category = new Category(null, name, macroCategory, emoji, userId, null, Instant.now(), null);
        Category savedCategory = categoryRepository.save(category);
        return toDTO(savedCategory);
    }

    public void updateCategoryMatchers(String userOwner, CategoryId categoryId, Set<String> matchers) {
        Category category = categoryRepository.findById(categoryId)
                .filter(c -> c.getUserOwner().equals(userOwner))
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        category.updateMatchers(matchers);
        categoryRepository.update(category);
    }

    public void addAutomaticSkip(String userOwner, List<String> regex) {
        automaticSkipRepository.add(userOwner, regex);
    }

    public void addAutomaticSkip(String userOwner, String regex) {
        automaticSkipRepository.add(userOwner, regex);
    }

    public void removeAutomaticSkip(String userOwner, String regex) {
        automaticSkipRepository.remove(userOwner, regex);
    }

    public Optional<AutomaticSkip> getAutomaticSkip(String userOwner) {
        return automaticSkipRepository.get(userOwner);
    }

    public boolean isAutomaticSkip(String userOwner, String description) {
        return automaticSkipRepository.get(userOwner)
                .map(automaticSkip -> automaticSkip.hasToSkip(description))
                .orElse(false);
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

    public Optional<Category> findMatchingCategory(String userId, String description) {
        return categoryRepository.findAllByUser(userId, 500)
                .stream()
                .filter(c -> c.matches(description))
                .findFirst();
    }


    public List<CategoryDTO> getCategoriesByIdsAndUser(List<CategoryId> categoryIds, String userOwner) {
        if (categoryIds.isEmpty()) {
            return List.of();
        }
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
