package com.marginalia.api.service;

import com.marginalia.api.security.LoginAttemptProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

/** Tracks failed login attempts in Redis and exposes the configured lockout threshold. */
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final String KEY_PREFIX = "marginalia:auth:login-attempts:";
    private static final RedisScript<Long> INCREMENT_SCRIPT = RedisScript.of("""
            local attempts = redis.call('INCR', KEYS[1])
            redis.call('EXPIRE', KEYS[1], ARGV[1])
            return attempts
            """, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final LoginAttemptProperties properties;

    /**
     * Determines whether an email has reached the failed-login threshold.
     *
     * @param email normalized email address
     * @return {@code true} when further login attempts must be rejected
     */
    public boolean isLocked(String email) {
        String attempts = redisTemplate.opsForValue().get(key(email));
        return attempts != null && Long.parseLong(attempts) >= properties.maxAttempts();
    }

    /**
     * Atomically increments an email's failed-attempt counter and refreshes its expiration.
     *
     * @param email normalized email address
     * @return updated failed-attempt count
     * @throws IllegalStateException if Redis does not return the updated count
     */
    public long recordFailure(String email) {
        Long attempts = redisTemplate.execute(
                INCREMENT_SCRIPT,
                List.of(key(email)),
                Long.toString(properties.lockDuration().toSeconds())
        );
        if (attempts == null) {
            throw new IllegalStateException("Redis did not return the login attempt count");
        }
        return attempts;
    }

    /**
     * Clears all recorded login failures for an email.
     *
     * @param email normalized email address
     */
    public void reset(String email) {
        redisTemplate.delete(key(email));
    }

    /**
     * Returns the failed-attempt threshold.
     *
     * @return maximum permitted failed attempts
     */
    public int maxAttempts() {
        return properties.maxAttempts();
    }

    private String key(String email) {
        return KEY_PREFIX + email;
    }
}
