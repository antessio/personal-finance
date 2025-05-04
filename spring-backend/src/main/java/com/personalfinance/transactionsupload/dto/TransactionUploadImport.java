package com.personalfinance.transactionsupload.dto;

import lombok.Data;

@Data
public class TransactionUploadImport {
    private String sourceType;
    private String filePath;
}
