package antessio.personalfinance.infrastructure.web.controller;

import antessio.personalfinance.domain.model.Category;
import antessio.personalfinance.domain.service.CategoryService;
import antessio.personalfinance.infrastructure.persistence.mapper.CategoryMapper;
import antessio.personalfinance.infrastructure.persistence.repository.CategorySpringDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CategorySpringDataRepository categorySpringDataRepository;
    private final CategoryMapper categoryMapper;

    @GetMapping
    public ResponseEntity<List<Category>> getCategories(@RequestParam String userOwner) {
        return ResponseEntity.ok(
                categorySpringDataRepository.findByUserOwner(userOwner)
                                            .stream()
                                            .map(categoryMapper::toDomain)
                                            .collect(Collectors.toList())
        );
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        return ResponseEntity.ok(categoryService.createCategory(category));
    }

    @PutMapping("/{id}/matchers")
    public ResponseEntity<Category> updateMatchers(
            @PathVariable Long id,
            @RequestBody List<String> matchers) {
        return ResponseEntity.ok(categoryService.updateCategoryMatchers(id, matchers););
    }
} 