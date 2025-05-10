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
    Boolean skip;
    String source;
    int limit;
    TransactionId cursor;

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

}
