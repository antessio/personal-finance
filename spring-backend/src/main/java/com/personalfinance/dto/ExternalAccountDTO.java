package com.personalfinance.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExternalAccountDTO {
    private Long id;
    private String name;
    private String type;
    private String institution;
    private String accountNumber;
    private LocalDateTime insertedAt;
    private LocalDateTime updatedAt;
} 