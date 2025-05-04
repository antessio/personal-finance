package com.personalfinance.transaction.controller;

import com.personalfinance.security.SecurityUtils;
import com.personalfinance.transaction.dto.CreateTransactionDTO;
import com.personalfinance.transaction.dto.TransactionDTO;
import com.personalfinance.transaction.service.TransactionService;
import com.personalfinance.user.model.User;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@Valid @RequestBody CreateTransactionDTO transactionDTO) {
        User user = SecurityUtils.getAuthenticatedUser();
        return ResponseEntity.ok(transactionService.createTransaction(transactionDTO, user.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDTO> updateTransaction(@PathVariable Long id,
            @Valid @RequestBody TransactionDTO transactionDTO) {
        User user = SecurityUtils.getAuthenticatedUser();
        return ResponseEntity.ok(transactionService.updateTransaction(transactionDTO, user.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID id) {
        User user = SecurityUtils.getAuthenticatedUser();
        transactionService.deleteTransaction(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransaction(@PathVariable UUID id) {
        User user = SecurityUtils.getAuthenticatedUser();
        return ResponseEntity.ok(transactionService.getTransaction(id, user.getId()));
    }

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getTransactions(@RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate, @RequestParam("skip") Boolean skip,
            @RequestParam("categoryId") Long categoryId) {
        User user = SecurityUtils.getAuthenticatedUser();
        return ResponseEntity
                .ok(transactionService.getTransactionsByUser(user.getId(), startDate, endDate, skip, categoryId));
    }

}