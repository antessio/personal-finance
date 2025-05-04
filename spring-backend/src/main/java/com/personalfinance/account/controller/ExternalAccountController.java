package com.personalfinance.account.controller;

import com.personalfinance.account.dto.ExternalAccountDTO;
import com.personalfinance.account.service.ExternalAccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class ExternalAccountController {

    private final ExternalAccountService externalAccountService;

    public ExternalAccountController(ExternalAccountService externalAccountService) {
        this.externalAccountService = externalAccountService;
    }

    @GetMapping
    public ResponseEntity<List<ExternalAccountDTO>> getAllAccounts() {
        return ResponseEntity.ok(externalAccountService.getAllAccounts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExternalAccountDTO> getAccount(@PathVariable Long id) {
        return ResponseEntity.ok(externalAccountService.getAccountById(id));
    }

    @PostMapping
    public ResponseEntity<ExternalAccountDTO> createAccount(@Valid @RequestBody ExternalAccountDTO accountDTO) {
        return ResponseEntity.ok(externalAccountService.createAccount(accountDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExternalAccountDTO> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody ExternalAccountDTO accountDTO) {
        return ResponseEntity.ok(externalAccountService.updateAccount(id, accountDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        externalAccountService.deleteAccount(id);
        return ResponseEntity.ok().build();
    }
} 