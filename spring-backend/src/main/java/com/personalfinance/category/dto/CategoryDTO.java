package com.personalfinance.category.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private String color;
    private LocalDateTime insertedAt;
    private LocalDateTime updatedAt;
} 