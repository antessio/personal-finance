package com.personalfinance.transaction.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateTransactionDTO {
    
    private String description;
    private BigDecimal amount;
    private LocalDateTime date;
    private Long categoryId;
    private String source;
} 