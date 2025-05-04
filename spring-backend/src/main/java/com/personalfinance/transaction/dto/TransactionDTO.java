package com.personalfinance.transaction.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
public class TransactionDTO {
    private UUID id;
    private String description;
    private BigDecimal amount;
    private LocalDate date;
    private String uniqueId;
    private String source;
    private Boolean skip;
    private Set<Long> categoryIds;
    private LocalDateTime insertedAt;
    private LocalDateTime updatedAt;
} 