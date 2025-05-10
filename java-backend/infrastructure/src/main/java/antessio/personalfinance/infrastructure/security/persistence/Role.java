package antessio.personalfinance.infrastructure.security.persistence;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Role {

    public static final String ADMIN = "ROLE_ADMIN";

    @Id
    @Column(name="id", nullable = false)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // To prevent circular serialization
    private User user;

    @Column(nullable = false)
    private String role;

}