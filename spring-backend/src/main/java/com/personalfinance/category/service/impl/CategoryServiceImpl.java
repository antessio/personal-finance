package com.personalfinance.category.service.impl;

import com.personalfinance.category.dto.CategoryDTO;
import com.personalfinance.category.dto.CreateCategoryDTO;
import com.personalfinance.category.model.Category;
import com.personalfinance.category.model.MacroCategoryEnum;
import com.personalfinance.category.persistence.CategoryRepository;
import com.personalfinance.category.service.CategoryService;
import com.personalfinance.user.model.User;
import com.personalfinance.user.persistence.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public CategoryDTO createCategory(CreateCategoryDTO createCategoryDTO, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Category category = new Category();
        category.setName(createCategoryDTO.getName());
        category.setMacroCategory(createCategoryDTO.getMacroCategory());
        category.setUser(user);

        Category savedCategory = categoryRepository.save(category);
        return convertToDTO(savedCategory);
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO, Long userId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.setName(categoryDTO.getName());
        category.setMacroCategory(MacroCategoryEnum.valueOf(categoryDTO.getMacroCategory()));

        Category updatedCategory = categoryRepository.save(category);
        return convertToDTO(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id, Long userId) {
        categoryRepository.deleteById(id);
    }

    @Override
    public CategoryDTO getCategory(Long id, Long userId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return convertToDTO(category);
    }

    @Override
    public List<CategoryDTO> getCategoriesByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return categoryRepository.findByUser(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setMacroCategory(category.getMacroCategory().name());
        dto.setInsertedAt(category.getInsertedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        return dto;
    }
} 