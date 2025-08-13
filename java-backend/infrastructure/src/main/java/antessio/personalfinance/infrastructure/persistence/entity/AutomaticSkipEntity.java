package antessio.personalfinance.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "automatic_skip")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AutomaticSkipEntity {
    @Id
    @Column(name = "skip_matcher", nullable = false, length = 255)
    private String skipMatcher;

    @Column(name = "user_owner", nullable = false, length = 255)
    private String userOwner;
}

