package com.personalfinance.transactionsupload.controller;

import com.personalfinance.transactionsupload.dto.TransactionUploadDTO;
import com.personalfinance.transactionsupload.dto.TransactionUploadImport;
import com.personalfinance.transactionsupload.service.TransactionUploadService;
import com.personalfinance.user.model.User;
import com.personalfinance.user.persistence.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transaction-uploads")
public class TransactionUploadController {

    private final TransactionUploadService transactionUploadService;
    private final UserRepository userRepository;

    public TransactionUploadController(
            TransactionUploadService transactionUploadService,
            UserRepository userRepository) {
        this.transactionUploadService = transactionUploadService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<TransactionUploadDTO>> getUploads() {
        Long userId = getAuthenticatedUserId();
        return ResponseEntity.ok(transactionUploadService.getUploadsByUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionUploadDTO> getUpload(@PathVariable Long id) {
        Long userId = getAuthenticatedUserId();
        return transactionUploadService.getUpload(id, userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/import")
    public ResponseEntity<TransactionUploadDTO> importTransactions(@Valid @RequestBody TransactionUploadImport transactionUploadImport) {
        Long userId = getAuthenticatedUserId();
        return ResponseEntity.ok(transactionUploadService.importTransactions(transactionUploadImport, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUpload(@PathVariable Long id) {
        Long userId = getAuthenticatedUserId();
        transactionUploadService.deleteUpload(id, userId);
        return ResponseEntity.ok().build();
    }

    private Long getAuthenticatedUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
} 