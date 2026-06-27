package antessio.personalfinance.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "category_matchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryMatcherEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "matcher")
    private String matcher;

    @Column(name="year")
    private Integer year;

    // THIS IS THE FIX: The Back-Reference mapping
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryMatcherEntity that)) return false;
        // Two matchers are the same if they have the same string value
        return Objects.equals(matcher, that.matcher) && Objects.equals(year, that.year) && Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matcher, year, category);
    }
}
