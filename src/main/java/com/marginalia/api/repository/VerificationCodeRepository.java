package com.marginalia.api.repository;

import com.marginalia.api.domain.VerificationCode;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/** Provides persistence and atomic consumption queries for email verification codes. */
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, UUID> {

    /**
     * Locks and retrieves the newest unused code matching a user and value.
     *
     * @param userId user identifier
     * @param code verification code value
     * @return matching verification code, if present
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<VerificationCode> findFirstByUserIdAndCodeAndUsedFalseOrderByExpiresAtDesc(
            UUID userId,
            String code
    );

    /**
     * Marks every outstanding verification code for a user as used.
     *
     * @param userId user identifier
     */
    @Modifying
    @Query("update VerificationCode code set code.used = true "
            + "where code.userId = :userId and code.used = false")
    void markUnusedCodesAsUsed(@Param("userId") UUID userId);
}
