package com.marginalia.api.service;

import com.marginalia.api.exception.InvalidPasswordException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/** Enforces constraints imposed by BCrypt's maximum UTF-8 password length. */
@Component
public class PasswordPolicy {

    private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;

    /**
     * Validates that a password can be safely processed by BCrypt.
     *
     * @param password password to validate
     * @throws InvalidPasswordException if the UTF-8 representation exceeds BCrypt's limit
     */
    public void validate(String password) {
        if (exceedsBcryptLimit(password)) {
            throw new InvalidPasswordException();
        }
    }

    /**
     * Tests whether a password exceeds BCrypt's maximum input length.
     *
     * @param password password to measure as UTF-8
     * @return {@code true} when the password exceeds seventy-two bytes
     */
    public boolean exceedsBcryptLimit(String password) {
        return password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_PASSWORD_BYTES;
    }
}
