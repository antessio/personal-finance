package antessio.personalfinance.domain.model;

import java.time.Instant;
import java.util.Set;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public class Category {

    private CategoryId id;
    private String name;
    private MacroCategoryEnum macroCategory;
    private CategoryType type;
    private String emoji;
    private String userOwner;
    private Set<String> matchers;
    private Instant insertedAt;
    private Instant updatedAt;

    public void setCategoryType(CategoryType type) {
        this.type = type;
        this.updatedAt = Instant.now();
    }

    public void updateMatchers(Set<String> matchers) {
        this.matchers = matchers;
        this.updatedAt = Instant.now();
    }

    public boolean matches(String description) {
        if (matchers == null || matchers.isEmpty()) {
            return false;
        }
        return matchers.stream()
                .map(Pattern::compile)
                .anyMatch(pattern -> pattern.matcher(description).find());
    }



}
