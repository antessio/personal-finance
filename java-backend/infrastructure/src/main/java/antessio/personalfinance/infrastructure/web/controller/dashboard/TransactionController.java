package antessio.personalfinance.infrastructure.web.controller.dashboard;

import antessio.personalfinance.domain.dto.SavingsExportDTO;
import antessio.personalfinance.domain.dto.TransactionDTO;
import antessio.personalfinance.domain.dto.TransactionExportDTO;
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
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<PaginatedResult<TransactionDTO>> getTransactions(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Boolean skip,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Boolean uncategorized) {
        User user = securityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        limit = Optional.ofNullable(limit).orElse(20);
        List<TransactionDTO> results = transactionService.findTransactions(
                TransactionsQueryDTO.builder()
                        .month(Optional.ofNullable(targetDate)
                                .map(date -> YearMonth.of(date.getYear(), date.getMonthValue()))
                                .orElse(null))
                        .skip(skip)
                        .categoryId(Optional.ofNullable(categoryId).map(CategoryId::new).orElse(null))
                        .cursor(Optional.ofNullable(cursor).map(TransactionId::fromString).orElse(null))
                        .uncategorized(uncategorized)
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
        User user = securityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        transactionService.skipTransactions(List.of(TransactionId.fromString(id)), user.getUsername());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{id}/include")
    public ResponseEntity<Void> includeTransaction(@PathVariable String id) {
        User user = securityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        transactionService.includeTransactions(List.of(TransactionId.fromString(id)), user.getUsername());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{id}/processCategory")
    public ResponseEntity<Void> processCategory(
            @PathVariable String id) {
        User user = securityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        transactionService.processCategories(List.of(TransactionId.fromString(id)), user.getUsername());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/processCategoriesAll")
    public ResponseEntity<Void> processCategoriesAll() {
        User user = securityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        transactionService.processCategoriesAll(user.getUsername());
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/{id}/category")
    public ResponseEntity<Void> assignCategory(
            @PathVariable String id,
            @RequestParam Long categoryId) {
        User user = securityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        transactionService.assignCategory(List.of(TransactionId.fromString(id)), new CategoryId(categoryId), user.getUsername());
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/export/transactions")
    public ResponseEntity<String> exportTransactions(
            @RequestParam() String yearMonth) {
        User user = securityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        YearMonth ym = YearMonth.parse(yearMonth);
        List<TransactionExportDTO> transactions = transactionService.exportTransactions(ym, user.getUsername());
        StringBuilder csv = new StringBuilder();
        csv.append("date,type,macro_category,category,currency,amount,description\n");
        for (TransactionExportDTO t : transactions) {
            csv.append(t.getDate()).append(",")
               .append(t.getType()).append(",")
               .append(t.getMacroCategory()).append(",")
               .append(t.getCategory()).append(",")
               .append(t.getCurrency()).append(",")
               .append(t.getAmount()).append(",")
               .append(t.getDescription().replaceAll("[\r\n]", " ")).append("\n");
        }
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .body(csv.toString());
    }
    @GetMapping("/export/savings")
    public ResponseEntity<String> exportSavings(
            @RequestParam() String yearMonth) {
        User user = securityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        YearMonth ym = YearMonth.parse(yearMonth);
        List<SavingsExportDTO> transactions = transactionService.exportSavings(ym, user.getUsername());
        StringBuilder csv = new StringBuilder();
        csv.append("date,category,currency,amount\n");
        for (SavingsExportDTO t : transactions) {
            csv.append(t.getDate()).append(",")
               .append(t.getCategory()).append(",")
               .append(t.getCurrency()).append(",")
               .append(t.getAmount()).append(",")
               .append("\n");
        }
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .body(csv.toString());
    }

}

