package antessio.personalfinance.infrastructure.web.controller.dashboard;

import antessio.personalfinance.domain.dto.TransactionDTO;
import antessio.personalfinance.domain.dto.TransactionsQueryDTO;
import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.model.TransactionId;
import antessio.personalfinance.domain.service.TransactionService;
import antessio.personalfinance.infrastructure.security.persistence.User;
import antessio.personalfinance.infrastructure.security.service.SecurityUtils;
import antessio.personalfinance.infrastructure.web.controller.common.PaginatedResult;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<PaginatedResult<TransactionDTO>> getTransactions(
            @RequestParam("limit") Integer limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Boolean skip,
            @RequestParam(required = false) String cursor) {
        User user = SecurityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        limit = Optional.ofNullable(limit).orElse(20);
        List<TransactionDTO> results = transactionService.findTransactions(
                TransactionsQueryDTO.builder()
                        .month(YearMonth.of(targetDate.getYear(), targetDate.getMonthValue()))
                        .skip(skip)
                        .categoryId(Optional.ofNullable(categoryId).map(CategoryId::new).orElse(null))
                        .cursor(Optional.ofNullable(cursor).map(TransactionId::fromString).orElse(null))
                        .source(source)
                        .userOwner(user.getUsername())
                        .limit(limit + 1)
                        .build()
        );
        
        return ResponseEntity.ok(
                PaginatedResult.from(results, limit)
        );
    }

    @PostMapping("/{id}/skip")
    public ResponseEntity<Void> skipTransaction(@PathVariable String id) {
        User user = SecurityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        transactionService.skipTransactions(List.of(TransactionId.fromString(id)), user.getUsername());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{id}/include")
    public ResponseEntity<Void> includeTransaction(@PathVariable String id) {
        User user = SecurityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        transactionService.includeTransactions(List.of(TransactionId.fromString(id)), user.getUsername());
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/{id}/category")
    public ResponseEntity<Void> assignCategory(
            @PathVariable String id,
            @RequestParam Long categoryId) {
        User user = SecurityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        transactionService.assignCategory(List.of(TransactionId.fromString(id)), new CategoryId(categoryId), user.getUsername());
        return ResponseEntity.accepted().build();
    }
} 