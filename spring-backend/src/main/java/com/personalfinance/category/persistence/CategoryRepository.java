package com.personalfinance.category.persistence;

import com.personalfinance.category.model.Category;
import com.personalfinance.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUser(User user);
    boolean existsByNameAndUser(String name, User user);
} 