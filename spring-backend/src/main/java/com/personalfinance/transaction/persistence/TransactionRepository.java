package com.personalfinance.transaction.persistence;

import com.personalfinance.account.model.ExternalAccount;
import com.personalfinance.category.model.Category;
import com.personalfinance.transaction.model.Transaction;
import com.personalfinance.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUser(User user);
    List<Transaction> findByCategory(Category category);
    List<Transaction> findByExternalAccount(ExternalAccount account);
    List<Transaction> findByUserAndDateBetween(User user, LocalDateTime startDate, LocalDateTime endDate);
} 