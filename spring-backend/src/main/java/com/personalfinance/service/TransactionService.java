package com.personalfinance.service;

import com.personalfinance.dto.TransactionDTO;
import java.util.List;

public interface TransactionService {
    TransactionDTO createTransaction(TransactionDTO transactionDTO);
    TransactionDTO updateTransaction(Long id, TransactionDTO transactionDTO);
    void deleteTransaction(Long id);
    TransactionDTO getTransaction(Long id);
    List<TransactionDTO> getTransactionsByUser();
    List<TransactionDTO> getTransactionsByCategory(Long categoryId);
    List<TransactionDTO> getTransactionsByExternalAccount(Long accountId);
} 