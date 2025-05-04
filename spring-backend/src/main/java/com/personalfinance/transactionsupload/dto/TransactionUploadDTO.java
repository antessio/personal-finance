package com.personalfinance.transactionsupload.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TransactionUploadDTO {
    private Long id;
    private String sourceType;
    private String filePath;
    private String status;
    private LocalDateTime insertedAt;
    private LocalDateTime updatedAt;
} 