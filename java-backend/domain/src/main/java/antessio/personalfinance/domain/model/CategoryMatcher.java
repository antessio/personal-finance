package antessio.personalfinance.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CategoryMatcher {
    private String matcher;
    private Integer year;

    public static CategoryMatcher of(String matcher){
        return new CategoryMatcher(matcher, null);
    }

    public static CategoryMatcher of(String matcher, Integer year) {
        return new CategoryMatcher(matcher, year);
    }
}
