package com.personalfinance.transaction.persistence;

import com.personalfinance.category.model.Category;
import com.personalfinance.transaction.model.Transaction;
import com.personalfinance.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("SELECT t FROM Transaction t WHERE t.user = :user " +
           "#{(#skip != null) ? 'AND t.skip = :skip' : ''} " +
           "#{(#startDate != null) ? 'AND t.date >= :startDate' : ''} " +
           "#{(#endDate != null) ? 'AND t.date <= :endDate' : ''} " +
           "#{(#category != null) ? 'AND :category IN (SELECT c FROM t.categories c WHERE c.id = :categoryId)' : ''} " +
           "ORDER BY t.date DESC")
    List<Transaction> findByUser(
            @Param("user") User user,
            @Param("skip") Boolean skip,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryId") Long categoryId);

    List<Transaction> findByCategoriesContaining(Category category);
} 