package antessio.personalfinance.domain.dto;

import java.time.Instant;
import java.util.Set;

import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.model.MacroCategoryEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CategoryDTO {
    private CategoryId id;
    private String name;
    private MacroCategoryEnum macroCategory;
    private String userOwner;
    private Set<String> matchers;
    private Instant insertedAt;
    private Instant updatedAt;

}
