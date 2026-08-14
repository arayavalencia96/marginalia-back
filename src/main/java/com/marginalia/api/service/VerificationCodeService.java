package com.marginalia.api.service;

import com.marginalia.api.domain.User;
import com.marginalia.api.domain.VerificationCode;
import com.marginalia.api.exception.InvalidVerificationCodeException;
import com.marginalia.api.repository.VerificationCodeRepository;
import com.marginalia.api.security.VerificationCodeProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

/** Issues, delivers, consumes, and invalidates short-lived email verification codes. */
@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private static final int CODE_BOUND = 1_000_000;

    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;
    private final VerificationCodeProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Invalidates previous codes, persists a new six-digit code, and emails it to the user.
     *
     * @param user user receiving the verification code
     * @throws com.marginalia.api.exception.EmailDeliveryException if the code email cannot be delivered
     */
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

        emailService.sendVerificationCode(user.getEmail(), code);
    }

    /**
     * Consumes the newest matching unexpired verification code.
     *
     * @param user user whose email is being verified
     * @param code verification code supplied by the user
     * @throws InvalidVerificationCodeException if the code is unknown, used, or expired
     */
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

    /**
     * Marks every outstanding verification code for a user as used.
     *
     * @param userId identifier of the user
     */
    @Transactional
    public void invalidateForUser(UUID userId) {
        verificationCodeRepository.markUnusedCodesAsUsed(userId);
    }
}
