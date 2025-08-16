package antessio.personalfinance.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Transaction {

    private TransactionId id;

    private LocalDate date;

    private BigDecimal amount;

    private String description;

    private String uniqueId;

    private String source;

    private Boolean skip;

    private String userOwner;

    private CategoryId categoryId;

    private LocalDateTime insertedAt;

    private LocalDateTime updatedAt;

    private TransactionImportId transactionImportId;

    public void skip(){
        this.skip = true;
        updated();
    }
    public void include(){
        this.skip = false;
        updated();
    }
    public void assignCategory(CategoryId category) {
        this.categoryId = category;
        updated();
    }

    private void updated() {
        this.updatedAt = LocalDateTime.now();
    }

}
