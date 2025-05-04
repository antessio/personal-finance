package com.personalfinance.transactionsupload.persistence;

import com.personalfinance.transactionsupload.model.TransactionUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionUploadRepository extends JpaRepository<TransactionUpload, Long> {
    List<TransactionUpload> findByUserId(Long userId);
} 