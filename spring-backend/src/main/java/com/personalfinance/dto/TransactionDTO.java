package com.personalfinance.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDTO {
    private Long id;
    private String description;
    private BigDecimal amount;
    private LocalDateTime date;
    private String type;
    private Long categoryId;
    private String categoryName;
    private String categoryColor;
    private Long externalAccountId;
    private String externalAccountName;
    private LocalDateTime insertedAt;
    private LocalDateTime updatedAt;
} 