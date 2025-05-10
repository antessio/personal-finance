package antessio.personalfinance.infrastructure.web.controller.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
@AllArgsConstructor
@Getter
public class PaginatedResult<T> {
    private List<T> data;
    private boolean hasNext;

    public static <T> PaginatedResult<T> from(List<T> results, Integer limit){
        return new PaginatedResult<>(
                results.subList(0, limit),
                results.size() > limit
        );
    }
}
