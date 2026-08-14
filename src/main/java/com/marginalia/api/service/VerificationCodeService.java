package com.marginalia.api.service;

import com.marginalia.api.domain.User;
import com.marginalia.api.domain.VerificationCode;
import com.marginalia.api.exception.InvalidVerificationCodeException;
import com.marginalia.api.repository.VerificationCodeRepository;
import com.marginalia.api.security.VerificationCodeProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationCodeService {

    private static final int CODE_BOUND = 1_000_000;

    private final VerificationCodeRepository verificationCodeRepository;
    private final VerificationCodeProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void issue(User user) {
        verificationCodeRepository.markUnusedCodesAsUsed(user.getId());
        String code = String.format(Locale.ROOT, "%06d", secureRandom.nextInt(CODE_BOUND));
        VerificationCode verificationCode = VerificationCode.builder()
                .userId(user.getId())
                .code(code)
                .expiresAt(Instant.now().plus(properties.codeExpiration()))
                .used(false)
                .build();
        verificationCodeRepository.save(verificationCode);

        log.info("Email verification code for {}: {}", user.getEmail(), code);
    }

    @Transactional
    public void consume(User user, String code) {
        VerificationCode verificationCode = verificationCodeRepository
                .findFirstByUserIdAndCodeAndUsedFalseOrderByExpiresAtDesc(user.getId(), code)
                .orElseThrow(InvalidVerificationCodeException::new);

        if (!verificationCode.getExpiresAt().isAfter(Instant.now())) {
            throw new InvalidVerificationCodeException();
        }

        verificationCode.setUsed(true);
        verificationCodeRepository.save(verificationCode);
    }

    @Transactional
    public void invalidateForUser(UUID userId) {
        verificationCodeRepository.markUnusedCodesAsUsed(userId);
    }
}
