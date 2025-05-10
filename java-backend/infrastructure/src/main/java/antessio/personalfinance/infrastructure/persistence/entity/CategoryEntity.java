package antessio.personalfinance.infrastructure.persistence.entity;

import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.model.MacroCategoryEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEntity {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "macro_category", nullable = false)
    private MacroCategoryEnum macroCategory;

    @Column(name = "user_owner", nullable = false)
    private String userOwner;

    @ElementCollection
    @CollectionTable(name = "category_matchers", joinColumns = @JoinColumn(name = "category_id"))
    @Column(name = "matcher")
    private Set<String> matchers;

    @Column(name = "inserted_at", nullable = false)
    private Instant insertedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public CategoryId getCategoryId() {
        return new CategoryId(id);
    }
} 