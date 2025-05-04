package com.personalfinance.transaction.service;

import com.personalfinance.transaction.dto.CreateTransactionDTO;
import com.personalfinance.transaction.dto.TransactionDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
public interface TransactionService {
    TransactionDTO createTransaction(CreateTransactionDTO createTransactionDTO, Long userId);
    TransactionDTO updateTransaction(TransactionDTO transactionDTO, Long userId);
    void deleteTransaction(UUID id, Long userId);
    TransactionDTO getTransaction(UUID id, Long userId);
    List<TransactionDTO> getTransactionsByUser(Long userId, LocalDate startDate, LocalDate endDate, Boolean skip, Long categoryId);
} 