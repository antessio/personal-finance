package antessio.personalfinance.domain.dto;

import antessio.personalfinance.domain.model.MacroCategoryEnum;

public record CreateCategoryDTO(String userId,String name,MacroCategoryEnum macroCategory){
}
