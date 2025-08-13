package antessio.personalfinance.infrastructure.web.controller.dashboard;

import antessio.personalfinance.domain.dto.CategoryDTO;
import antessio.personalfinance.domain.dto.CreateCategoryDTO;
import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.service.CategoryService;
import antessio.personalfinance.infrastructure.security.persistence.User;
import antessio.personalfinance.infrastructure.security.service.SecurityUtils;
import antessio.personalfinance.infrastructure.web.controller.common.PaginatedResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<PaginatedResult<CategoryDTO>> getCategories(
            @RequestParam(value = "limit", defaultValue = "20") Integer limit,
            @RequestParam(value = "cursor", required = false) Long cursor
    ) {
        User user = SecurityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        limit = Optional.ofNullable(limit).orElse(20);
        List<CategoryDTO> results = categoryService.getAllCategories(user.getUsername(), limit + 1,
                Optional.ofNullable(cursor).map(CategoryId::new).orElse(null));
        return ResponseEntity.ok(
                PaginatedResult.from(results, limit)
        );
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CreateCategoryDTO category) {
         User user = SecurityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(categoryService.createCategory(user.getUsername(), category));
    }

    @PutMapping("/{id}/matchers")
    public ResponseEntity<Void> updateMatchers(
            @PathVariable Long id,
            @RequestBody List<String> matchers) {
         User user = SecurityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        categoryService.updateCategoryMatchers(user.getUsername(), new CategoryId(id), new HashSet<>(matchers));
        return ResponseEntity.accepted().build();
    }
    @PostMapping("/skip-matchers")
    public ResponseEntity<Void> updateSkipMatchers(
            @RequestBody List<String> skipMatchers) {
         User user = SecurityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        categoryService.addAutomaticSkip(user.getUsername(), skipMatchers);
        return ResponseEntity.accepted().build();
    }
} 