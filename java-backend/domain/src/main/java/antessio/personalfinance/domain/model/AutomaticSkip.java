package antessio.personalfinance.domain.model;

import java.util.Set;
import java.util.regex.Pattern;

public record AutomaticSkip(Set<String> matchers) {

    public boolean hasToSkip(String description) {
        if (matchers == null || matchers.isEmpty()) {
            return false;
        }
        return matchers.stream()
                .map(Pattern::compile)
                .anyMatch(pattern -> pattern.matcher(description).find());
    }


}
