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

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<VerificationCode> findFirstByUserIdAndCodeAndUsedFalseOrderByExpiresAtDesc(
            UUID userId,
            String code
    );

    @Modifying
    @Query("update VerificationCode code set code.used = true "
            + "where code.userId = :userId and code.used = false")
    void markUnusedCodesAsUsed(@Param("userId") UUID userId);
}
