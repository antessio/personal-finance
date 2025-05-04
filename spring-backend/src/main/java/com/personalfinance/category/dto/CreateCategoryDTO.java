package com.personalfinance.category.dto;

import com.personalfinance.category.model.MacroCategoryEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCategoryDTO {
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;

    @NotBlank(message = "Macro category is required")
    private MacroCategoryEnum macroCategory;
    
}