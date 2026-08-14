package com.marginalia.api.service;

import com.marginalia.api.security.LoginAttemptProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final String KEY_PREFIX = "auth:login-attempts:";
    private static final RedisScript<Long> INCREMENT_SCRIPT = RedisScript.of("""
            local attempts = redis.call('INCR', KEYS[1])
            redis.call('EXPIRE', KEYS[1], ARGV[1])
            return attempts
            """, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final LoginAttemptProperties properties;

    public boolean isLocked(String email) {
        String attempts = redisTemplate.opsForValue().get(key(email));
        return attempts != null && Long.parseLong(attempts) >= properties.maxAttempts();
    }

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

    public void reset(String email) {
        redisTemplate.delete(key(email));
    }

    public int maxAttempts() {
        return properties.maxAttempts();
    }

    private String key(String email) {
        return KEY_PREFIX + email;
    }
}
