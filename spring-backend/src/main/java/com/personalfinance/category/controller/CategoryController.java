package com.personalfinance.category.controller;

import com.personalfinance.category.dto.CategoryDTO;
import com.personalfinance.category.dto.CreateCategoryDTO;
import com.personalfinance.category.service.CategoryService;
import com.personalfinance.user.model.User;
import com.personalfinance.user.persistence.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final UserRepository userRepository;

    public CategoryController(CategoryService categoryService, UserRepository userRepository) {
        this.categoryService = categoryService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getCategories() {
        Long userId = getAuthenticatedUserId();
        return ResponseEntity.ok(categoryService.getCategoriesByUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategory(@PathVariable Long id) {
        Long userId = getAuthenticatedUserId();
        return ResponseEntity.ok(categoryService.getCategory(id, userId));
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CreateCategoryDTO createCategoryDTO) {
        Long userId = getAuthenticatedUserId();
        return ResponseEntity.ok(categoryService.createCategory(createCategoryDTO, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO categoryDTO) {
        Long userId = getAuthenticatedUserId();
        return ResponseEntity.ok(categoryService.updateCategory(id, categoryDTO, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        Long userId = getAuthenticatedUserId();
        categoryService.deleteCategory(id, userId);
        return ResponseEntity.ok().build();
    }

    private Long getAuthenticatedUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
} 