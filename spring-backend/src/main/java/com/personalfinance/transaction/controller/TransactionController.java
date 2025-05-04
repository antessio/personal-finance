package com.personalfinance.transaction.controller;

import com.personalfinance.transaction.dto.TransactionDTO;
import com.personalfinance.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@Valid @RequestBody TransactionDTO transactionDTO) {
        return ResponseEntity.ok(transactionService.createTransaction(transactionDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDTO> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionDTO transactionDTO) {
        return ResponseEntity.ok(transactionService.updateTransaction(id, transactionDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(transactionService.getTransactionsByCategory(categoryId));
    }

    @GetMapping("/by-account/{accountId}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(transactionService.getTransactionsByAccount(accountId));
    }
} 