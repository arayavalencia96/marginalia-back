package com.marginalia.api.repository;

import com.marginalia.api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Provides persistence, uniqueness checks, and lifecycle queries for users. */
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by email regardless of deletion state.
     *
     * @param email normalized email address
     * @return matching user, if present
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds an active user by email.
     *
     * @param email normalized email address
     * @return matching non-deleted user, if present
     */
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    /**
     * Tests whether an email is already registered.
     *
     * @param email normalized email address
     * @return whether the email exists
     */
    boolean existsByEmail(String email);

    /**
     * Tests whether a username is already registered.
     *
     * @param username username to test
     * @return whether the username exists
     */
    boolean existsByUsername(String username);

    /**
     * Permanently deletes accounts soft-deleted on or before a cutoff.
     *
     * @param cutoff latest deletion time eligible for purging
     * @return number of deleted users
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from User user where user.deletedAt is not null and user.deletedAt <= :cutoff")
    int deleteAllByDeletedAtBefore(@Param("cutoff") Instant cutoff);
}
