package antessio.personalfinance.infrastructure.web.controller;

import antessio.personalfinance.domain.model.Transaction;
import antessio.personalfinance.domain.service.TransactionService;
import antessio.personalfinance.infrastructure.persistence.mapper.TransactionMapper;
import antessio.personalfinance.infrastructure.persistence.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @GetMapping
    public ResponseEntity<List<Transaction>> getTransactions(
            @RequestParam String userOwner,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String categoryId) {
        
        List<Transaction> transactions;
        if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByUserOwnerAndDateBetween(userOwner, startDate, endDate)
                .stream()
                .map(transactionMapper::toDomain)
                .collect(Collectors.toList());
        } else if (categoryId != null) {
            transactions = transactionRepository.findByUserOwnerAndCategoryId(userOwner, categoryId)
                .stream()
                .map(transactionMapper::toDomain)
                .collect(Collectors.toList());
        } else {
            transactions = transactionRepository.findByUserOwner(userOwner)
                .stream()
                .map(transactionMapper::toDomain)
                .collect(Collectors.toList());
        }
        
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/{id}/skip")
    public ResponseEntity<Transaction> skipTransaction(@PathVariable String id) {
        return ResponseEntity.ok(transactionService.skipTransaction(id));
    }

    @PostMapping("/{id}/include")
    public ResponseEntity<Transaction> includeTransaction(@PathVariable String id) {
        return ResponseEntity.ok(transactionService.includeTransaction(id));
    }

    @PutMapping("/{id}/category")
    public ResponseEntity<Transaction> assignCategory(
            @PathVariable String id,
            @RequestParam String categoryId) {
        return ResponseEntity.ok(transactionService.assignCategory(id, categoryId));
    }
} 