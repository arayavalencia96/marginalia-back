package com.marginalia.api.repository;

import com.marginalia.api.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/** Provides persistence and revocation queries for hashed refresh tokens. */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Finds a refresh token by its SHA-256 hash.
     *
     * @param tokenHash refresh-token hash
     * @return matching token, if present
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Revokes every active refresh token issued to a user.
     *
     * @param userId user identifier
     */
    @Modifying
    @Query("update RefreshToken token set token.revoked = true "
            + "where token.userId = :userId and token.revoked = false")
    void revokeAllByUserId(@Param("userId") UUID userId);
}
