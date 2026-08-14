package com.marginalia.api.service;

import com.marginalia.api.exception.InvalidPasswordException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class PasswordPolicy {

    private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;

    public void validate(String password) {
        if (exceedsBcryptLimit(password)) {
            throw new InvalidPasswordException();
        }
    }

    public boolean exceedsBcryptLimit(String password) {
        return password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_PASSWORD_BYTES;
    }
}
