package com.personalfinance.category.service;

import com.personalfinance.category.dto.CategoryDTO;
import com.personalfinance.category.dto.CreateCategoryDTO;
import java.util.List;

public interface CategoryService {
    CategoryDTO createCategory(CreateCategoryDTO createCategoryDTO, Long userId);
    CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO, Long userId);
    void deleteCategory(Long id, Long userId);
    CategoryDTO getCategory(Long id, Long userId);
    List<CategoryDTO> getCategoriesByUser(Long userId);
} 