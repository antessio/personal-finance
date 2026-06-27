package antessio.personalfinance.infrastructure.web.controller.dashboard;

import antessio.personalfinance.domain.dto.CategoryDTO;
import antessio.personalfinance.domain.dto.CreateCategoryDTO;
import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.model.CategoryMatcher;
import antessio.personalfinance.domain.service.CategoryService;
import antessio.personalfinance.infrastructure.security.persistence.User;
import antessio.personalfinance.infrastructure.security.service.SecurityUtils;
import antessio.personalfinance.infrastructure.web.controller.common.PaginatedResult;
import antessio.personalfinance.infrastructure.web.controller.dto.CategoryMatchersUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<PaginatedResult<CategoryDTO>> getCategories(
            @RequestParam(value = "limit", defaultValue = "20") Integer limit,
            @RequestParam(value = "cursor", required = false) Long cursor
    ) {
        User user = securityUtils.getAuthenticatedUser();
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
        User user = securityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(categoryService.createCategory(user.getUsername(), category));
    }

    @PutMapping("/{id}/matchers")
    public ResponseEntity<Void> updateMatchers(
            @PathVariable Long id,
            @RequestBody CategoryMatchersUpdateDTO categoryMatchersUpdateDTO) {
        User user = securityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional.ofNullable(categoryMatchersUpdateDTO.matchers())
                .filter(Predicate.not(List::isEmpty))
                .map(m -> m.stream()
                        .map(cmDTO -> new CategoryMatcher(cmDTO.matcher(), cmDTO.year()))
                        .collect(Collectors.toSet()))
                .ifPresent(categoryMatchers -> categoryService.updateCategoryMatchers(
                        user.getUsername(),
                        new CategoryId(id),
                        categoryMatchers));

        return ResponseEntity.accepted().build();
    }

    @PostMapping("/skip-matchers")
    public ResponseEntity<Void> updateSkipMatchers(
            @RequestBody List<String> skipMatchers) {
        User user = securityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        categoryService.addAutomaticSkip(user.getUsername(), skipMatchers);
        return ResponseEntity.accepted().build();
    }
} 