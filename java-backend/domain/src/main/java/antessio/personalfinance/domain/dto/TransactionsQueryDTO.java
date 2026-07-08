package antessio.personalfinance.domain.dto;

import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.model.MacroCategoryEnum;
import antessio.personalfinance.domain.model.TransactionId;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.Optional;

@Builder
@Value
public class TransactionsQueryDTO {
    String userOwner;
    LocalDate fromDate;
    LocalDate toDate;
    CategoryId categoryId;
    Boolean uncategorized;
    Boolean skip;
    String source;
    int limit;
    TransactionId cursor;
    MacroCategoryEnum macroCategory;

    public TransactionsQueryDTO(String userOwner, LocalDate fromDate, LocalDate toDate, CategoryId categoryId, Boolean uncategorized, Boolean skip, String source, int limit, TransactionId cursor, MacroCategoryEnum macroCategory) {
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
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.categoryId = categoryId;
        this.uncategorized = uncategorized;
        this.skip = skip;
        this.source = source;
        this.limit = limit;
        this.cursor = cursor;
        this.macroCategory = macroCategory;
    }

    public Optional<LocalDate> getFromDate() {
        return Optional.ofNullable(fromDate);
    }

    public Optional<LocalDate> getToDate() {
        return Optional.ofNullable(toDate);
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

    public Optional<MacroCategoryEnum> getMacroCategory() {
        return Optional.ofNullable(macroCategory);
    }

}
