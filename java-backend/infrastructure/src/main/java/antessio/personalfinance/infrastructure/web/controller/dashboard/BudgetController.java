package antessio.personalfinance.infrastructure.web.controller.dashboard;

import antessio.personalfinance.domain.dto.BudgetDTO;
import antessio.personalfinance.domain.dto.CreateDefaultBudgetDTO;
import antessio.personalfinance.domain.dto.CreateMonthlyBudgetDTO;
import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.service.BudgetService;
import antessio.personalfinance.infrastructure.security.persistence.User;
import antessio.personalfinance.infrastructure.security.service.SecurityUtils;
import antessio.personalfinance.infrastructure.web.controller.dto.CreateBudgetDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final SecurityUtils securityUtils;
    private final BudgetService budgetService; // Assuming you have a BudgetService to handle business logic


    @GetMapping
    public ResponseEntity<List<BudgetDTO>> getBudgets(
            @RequestParam int year
    ) {
        User user = securityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(budgetService.getAllBudgets(user.getUsername(), year));
    }

    @PostMapping("/annual")
    public ResponseEntity<Void> createBudgetAnnual(
            @RequestBody CreateBudgetDTO budgetDTO
    ) {
        User user = securityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        budgetService.createDefaultBudget(user.getUsername(), new CreateDefaultBudgetDTO(
                user.getUsername(),
                new CategoryId(budgetDTO.categoryId()),
                budgetDTO.amount()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/monthly")
    public ResponseEntity<Void> createBudgetMonthly(
            @RequestBody CreateBudgetDTO budgetDTO
    ) {
        User user = securityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        budgetService.createMonthlyBudget(user.getUsername(), new CreateMonthlyBudgetDTO(
                user.getUsername(),
                new CategoryId(budgetDTO.categoryId()),
                budgetDTO.amount(),
                Optional.ofNullable(budgetDTO.yearMonth())
                        .map(YearMonth::parse)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid yearMonth format"))
        ));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<BudgetDTO>> createBudgetsBulk(
            @RequestBody List<CreateBudgetDTO> budgetDTOs
    ) {
        User user = securityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(budgetService.createDefaultBudgets(user.getUsername(), budgetDTOs
                .stream()
                .map(b -> new CreateDefaultBudgetDTO(
                        user.getUsername(),
                        new CategoryId(b.categoryId()),
                        b.amount()
                )).collect(Collectors.toSet())));

    }
}
