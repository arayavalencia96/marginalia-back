package com.marginalia.api.dto;

import com.marginalia.api.domain.User;

import java.util.UUID;

/**
 * Exposes the authenticated user's current profile and credential capabilities.
 *
 * @param id user identifier
 * @param email current email address
 * @param username current display username
 * @param passwordConfigured whether the account has a local password
 */
public record UserProfileResponse(
        UUID id,
        String email,
        String username,
        boolean passwordConfigured
) {

    /**
     * Creates a profile response from a persisted user.
     *
     * @param user persisted user
     * @return profile response
     */
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getPasswordHash() != null
        );
    }
}
