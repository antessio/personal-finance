package com.personalfinance.service;

import com.personalfinance.dto.ExternalAccountDTO;
import java.util.List;

public interface ExternalAccountService {
    ExternalAccountDTO createAccount(ExternalAccountDTO accountDTO);
    ExternalAccountDTO updateAccount(Long id, ExternalAccountDTO accountDTO);
    void deleteAccount(Long id);
    ExternalAccountDTO getAccount(Long id);
    List<ExternalAccountDTO> getAccountsByUser();
} 