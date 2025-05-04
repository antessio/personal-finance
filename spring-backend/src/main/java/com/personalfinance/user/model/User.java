package com.personalfinance.user.model;

import com.personalfinance.transaction.model.Transaction;
import com.personalfinance.category.model.Category;
import com.personalfinance.account.model.ExternalAccount;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "hashed_password", nullable = false)
    private String hashedPassword;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "confirmation_token")
    private String confirmationToken;

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @CreatedDate
    @Column(name = "inserted_at", nullable = false, updatable = false)
    private LocalDateTime insertedAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Transaction> transactions;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Category> categories;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ExternalAccount> externalAccounts;

    @PrePersist
    protected void onCreate() {
        insertedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 