package com.marginalia.api.service;

import com.marginalia.api.exception.InvalidPasswordException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordPolicyTest {

    private final PasswordPolicy passwordPolicy = new PasswordPolicy();

    @Test
    void acceptsPasswordWithinBcryptLimit() {
        assertThatCode(() -> passwordPolicy.validate("a".repeat(72))).doesNotThrowAnyException();
    }

    @Test
    void rejectsPasswordOverBcryptLimit() {
        assertThatThrownBy(() -> passwordPolicy.validate("a".repeat(73)))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void measuresUtf8BytesInsteadOfCharacters() {
        assertThat(passwordPolicy.exceedsBcryptLimit("á".repeat(37))).isTrue();
    }
}
