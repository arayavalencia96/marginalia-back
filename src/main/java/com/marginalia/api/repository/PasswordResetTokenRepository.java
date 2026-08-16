package com.marginalia.api.repository;

import com.marginalia.api.domain.PasswordResetToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/** Provides lifecycle queries for hashed, one-time password reset tokens. */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    /**
     * Finds and locks an unused token so that it can only be consumed once.
     *
     * @param tokenHash SHA-256 hash of the submitted token
     * @return the matching unused token, if any
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PasswordResetToken> findByTokenHashAndUsedFalse(String tokenHash);

    /**
     * Invalidates all remaining unused reset tokens for a user.
     *
     * @param userId user identifier
     */
    @Modifying
    @Query("update PasswordResetToken token set token.used = true "
            + "where token.userId = :userId and token.used = false")
    void invalidateUnusedForUser(@Param("userId") UUID userId);
}
