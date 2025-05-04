package com.personalfinance.category.service;

import com.personalfinance.category.dto.CategoryDTO;
import java.util.List;

public interface CategoryService {
    CategoryDTO createCategory(CategoryDTO categoryDTO);
    CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO);
    void deleteCategory(Long id);
    CategoryDTO getCategory(Long id);
    List<CategoryDTO> getCategoriesByUser();
} 