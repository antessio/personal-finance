package antessio.personalfinance.domain.dto;

import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.model.TransactionId;
import lombok.Builder;
import lombok.Value;

import java.time.YearMonth;
import java.util.Optional;

@Builder
@Value
public class TransactionsQueryDTO {
    String userOwner;
    YearMonth month;
    CategoryId categoryId;
    Boolean uncategorized;
    Boolean skip;
    String source;
    int limit;
    TransactionId cursor;

    public TransactionsQueryDTO(String userOwner, YearMonth month, CategoryId categoryId, Boolean uncategorized, Boolean skip, String source, int limit, TransactionId cursor) {
        if (userOwner == null) {
            throw new IllegalArgumentException("User owner cannot be null");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than 0");
        }
        if(uncategorized != null && categoryId != null) {
            throw new IllegalArgumentException("Cannot specify both uncategorized and categoryId");
        }
        this.userOwner = userOwner;
        this.month = month;
        this.categoryId = categoryId;
        this.uncategorized = uncategorized;
        this.skip = skip;
        this.source = source;
        this.limit = limit;
        this.cursor = cursor;
    }

    public Optional<YearMonth> getMonth() {
        return Optional.ofNullable(month);
    }

    public Optional<CategoryId> getCategoryId() {
        return Optional.ofNullable(categoryId);
    }

    public Optional<TransactionId> getCursor() {
        return Optional.ofNullable(cursor);
    }

    public Optional<Boolean> getSkip() {
        return Optional.ofNullable(skip);
    }

    public Optional<String> getSource() {
        return Optional.ofNullable(source);
    }

    public Optional<Boolean> getUncategorized() {
        return Optional.ofNullable(uncategorized);
    }

}
