package com.personalfinance.transactionsupload.service;

import com.personalfinance.transactionsupload.dto.TransactionUploadDTO;
import com.personalfinance.transactionsupload.dto.TransactionUploadImport;

import java.util.List;
import java.util.Optional;
public interface TransactionUploadService {
    TransactionUploadDTO importTransactions(TransactionUploadImport transactionUploadImport, Long userId);
    void deleteUpload(Long id, Long userId);
    Optional<TransactionUploadDTO> getUpload(Long id, Long userId);
    List<TransactionUploadDTO> getUploadsByUser(Long userId);
} 