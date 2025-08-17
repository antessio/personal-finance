package antessio.personalfinance.infrastructure.persistence.entity;

import antessio.personalfinance.domain.model.BudgetId;
import antessio.personalfinance.domain.model.CategoryId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "budgets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BudgetEntity {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "user_owner", nullable = false)
    private String userOwner;

    @Column(name = "year")
    private Integer year;

    @Column(name = "month")
    private Integer month;

    public BudgetId getBudgetId() {
        return BudgetId.fromString(id);
    }

    public CategoryId getCategoryIdObj() {
        return new CategoryId(categoryId);
    }
}

