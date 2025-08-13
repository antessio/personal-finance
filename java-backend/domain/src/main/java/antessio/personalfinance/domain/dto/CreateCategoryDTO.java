package antessio.personalfinance.domain.dto;

import antessio.personalfinance.domain.model.MacroCategoryEnum;

public record CreateCategoryDTO(String name,MacroCategoryEnum macroCategory, String emoji){
}
