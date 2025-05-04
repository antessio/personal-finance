package com.personalfinance.account.persistence;

import com.personalfinance.account.model.ExternalAccount;
import com.personalfinance.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExternalAccountRepository extends JpaRepository<ExternalAccount, Long> {
    List<ExternalAccount> findByUser(User user);
    boolean existsByNameAndUser(String name, User user);
    boolean existsByAccountNumberAndInstitution(String accountNumber, String institution);
} 