package antessio.personalfinance.infrastructure.web.controller.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
@AllArgsConstructor
@Getter
public class PaginatedResult<T> {
    private List<T> data;
    private boolean hasNext;

    public static <T> PaginatedResult<T> from(List<T> results, Integer limit){
        if (limit == null || limit <= 0) {
            throw new IllegalArgumentException("Limit must be a positive integer.");
        }
        if (results == null || results.isEmpty()) {
            return new PaginatedResult<>(Collections.emptyList(), false);
        }
        List<T> partialResults = new ArrayList<>(results);
        return new PaginatedResult<>(
                partialResults.subList(0, Math.min(limit, partialResults.size())),
                partialResults.size() > limit
        );
    }
}
