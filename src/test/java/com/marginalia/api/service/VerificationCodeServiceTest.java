package com.marginalia.api.service;

import com.marginalia.api.domain.User;
import com.marginalia.api.domain.VerificationCode;
import com.marginalia.api.exception.InvalidVerificationCodeException;
import com.marginalia.api.repository.VerificationCodeRepository;
import com.marginalia.api.security.VerificationCodeProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationCodeServiceTest {

    @Mock
    private VerificationCodeRepository repository;

    @Mock
    private EmailService emailService;

    private VerificationCodeService service;

    @BeforeEach
    void setUp() {
        service = new VerificationCodeService(repository, emailService, new VerificationCodeProperties(Duration.ofMinutes(15)));
    }

    @Test
    void issuesSixDigitCodeAndInvalidatesOlderCodes() {
        User user = User.builder().id(UUID.randomUUID()).email("user@example.com").build();
        when(repository.save(any(VerificationCode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.issue(user);

        verify(repository).markUnusedCodesAsUsed(user.getId());
        verify(emailService).sendVerificationCode(
                org.mockito.ArgumentMatchers.eq(user.getEmail()),
                org.mockito.ArgumentMatchers.matches("\\d{6}")
        );
    }

    @Test
    void consumesActiveCode() {
        User user = User.builder().id(UUID.randomUUID()).build();
        VerificationCode code = VerificationCode.builder().expiresAt(Instant.now().plusSeconds(60)).used(false).build();
        when(repository.findFirstByUserIdAndCodeAndUsedFalseOrderByExpiresAtDesc(user.getId(), "123456"))
                .thenReturn(Optional.of(code));

        service.consume(user, "123456");

        assertThat(code.isUsed()).isTrue();
        verify(repository).save(code);
    }

    @Test
    void rejectsMissingOrExpiredCode() {
        User user = User.builder().id(UUID.randomUUID()).build();
        when(repository.findFirstByUserIdAndCodeAndUsedFalseOrderByExpiresAtDesc(user.getId(), "123456"))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.consume(user, "123456"))
                .isInstanceOf(InvalidVerificationCodeException.class);

        VerificationCode expired = VerificationCode.builder().expiresAt(Instant.now().minusSeconds(1)).build();
        when(repository.findFirstByUserIdAndCodeAndUsedFalseOrderByExpiresAtDesc(user.getId(), "123456"))
                .thenReturn(Optional.of(expired));
        assertThatThrownBy(() -> service.consume(user, "123456"))
                .isInstanceOf(InvalidVerificationCodeException.class);
    }

    @Test
    void invalidatesOutstandingCodes() {
        UUID userId = UUID.randomUUID();

        service.invalidateForUser(userId);

        verify(repository).markUnusedCodesAsUsed(userId);
    }
}
