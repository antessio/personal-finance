package com.personalfinance.auth.repository;

import com.personalfinance.auth.model.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findByToken(String token);
    Optional<UserToken> findByTokenAndTokenType(String token, UserToken.TokenType tokenType);

    @Modifying
    @Query("DELETE FROM UserToken ut WHERE ut.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query(value = "INSERT INTO user_tokens (user_id, token, token_type, expires_at, inserted_at, updated_at) " +
            "VALUES (:userId, :token, :tokenType, :expiresAt, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            nativeQuery = true)
    void createToken(@Param("userId") Long userId,
                    @Param("token") String token,
                    @Param("tokenType") String tokenType,
                    @Param("expiresAt") String expiresAt);
} 