package antessio.personalfinance.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
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
    private Set<CategoryMatcher> matchers;
    private Instant insertedAt;
    private Instant updatedAt;

    public void setCategoryType(CategoryType type) {
        this.type = type;
        this.updatedAt = Instant.now();
    }

    public void updateMatchers(Set<CategoryMatcher> matchers) {
        this.matchers = matchers;
        this.updatedAt = Instant.now();
    }

    public boolean matches(Transaction transaction) {
        if (matchers == null || matchers.isEmpty()) {
            return false;
        }
        return matchers.stream()
                .anyMatch(cm -> matchYear(cm, transaction.getDate()) && matchDescription(cm, transaction.getDescription()));
    }

    private static boolean matchDescription(CategoryMatcher cm, String transactionDescription) {
        return Pattern.compile(cm.getMatcher()).matcher(transactionDescription).find();
    }

    private static Boolean matchYear(CategoryMatcher cm, LocalDate transactionDate) {
        return Optional.ofNullable(cm.getYear())
                .map(y -> transactionDate.getYear() == y)
                .orElse(true);
    }

    public boolean matches(String description) {
        if (matchers == null || matchers.isEmpty()) {
            return false;
        }
        return matchers.stream()
                .map(CategoryMatcher::getMatcher)
                .map(Pattern::compile)
                .anyMatch(pattern -> pattern.matcher(description).find());
    }



}
