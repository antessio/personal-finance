package com.personalfinance.account.service.impl;

import com.personalfinance.account.dto.ExternalAccountDTO;
import com.personalfinance.account.model.ExternalAccount;
import com.personalfinance.account.persistence.ExternalAccountRepository;
import com.personalfinance.account.service.ExternalAccountService;
import com.personalfinance.user.model.User;
import com.personalfinance.user.persistence.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExternalAccountServiceImpl implements ExternalAccountService {

    private final ExternalAccountRepository accountRepository;
    private final UserRepository userRepository;

    public ExternalAccountServiceImpl(ExternalAccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ExternalAccountDTO createAccount(ExternalAccountDTO accountDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ExternalAccount account = new ExternalAccount();
        account.setName(accountDTO.getName());
        account.setType(accountDTO.getType());
        account.setInstitution(accountDTO.getInstitution());
        account.setAccountNumber(accountDTO.getAccountNumber());
        account.setUser(user);

        ExternalAccount savedAccount = accountRepository.save(account);
        return convertToDTO(savedAccount);
    }

    @Override
    @Transactional
    public ExternalAccountDTO updateAccount(Long id, ExternalAccountDTO accountDTO) {
        ExternalAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setName(accountDTO.getName());
        account.setType(accountDTO.getType());
        account.setInstitution(accountDTO.getInstitution());
        account.setAccountNumber(accountDTO.getAccountNumber());

        ExternalAccount updatedAccount = accountRepository.save(account);
        return convertToDTO(updatedAccount);
    }

    @Override
    @Transactional
    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }

    @Override
    public ExternalAccountDTO getAccount(Long id) {
        ExternalAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return convertToDTO(account);
    }

    @Override
    public List<ExternalAccountDTO> getAccountsByUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return accountRepository.findByUser(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ExternalAccountDTO convertToDTO(ExternalAccount account) {
        ExternalAccountDTO dto = new ExternalAccountDTO();
        dto.setId(account.getId());
        dto.setName(account.getName());
        dto.setType(account.getType());
        dto.setInstitution(account.getInstitution());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setInsertedAt(account.getInsertedAt());
        dto.setUpdatedAt(account.getUpdatedAt());
        return dto;
    }
} 