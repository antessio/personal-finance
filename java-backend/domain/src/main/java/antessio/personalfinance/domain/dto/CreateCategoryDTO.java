package antessio.personalfinance.domain.dto;

import antessio.personalfinance.domain.model.CategoryType;
import antessio.personalfinance.domain.model.MacroCategoryEnum;

public record CreateCategoryDTO(String name, MacroCategoryEnum macroCategory, CategoryType type, String emoji){
}
