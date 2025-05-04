package com.personalfinance.account.api;

import com.personalfinance.account.dto.ExternalAccountDTO;
import com.personalfinance.account.service.ExternalAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class ExternalAccountController {

    private final ExternalAccountService accountService;

    public ExternalAccountController(ExternalAccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<ExternalAccountDTO> createAccount(@RequestBody ExternalAccountDTO accountDTO) {
        return ResponseEntity.ok(accountService.createAccount(accountDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExternalAccountDTO> updateAccount(@PathVariable Long id, @RequestBody ExternalAccountDTO accountDTO) {
        return ResponseEntity.ok(accountService.updateAccount(id, accountDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExternalAccountDTO> getAccount(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccount(id));
    }

    @GetMapping
    public ResponseEntity<List<ExternalAccountDTO>> getAccounts() {
        return ResponseEntity.ok(accountService.getAccountsByUser());
    }
} 