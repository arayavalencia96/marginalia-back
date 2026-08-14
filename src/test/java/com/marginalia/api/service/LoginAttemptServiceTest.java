package com.marginalia.api.service;

import com.marginalia.api.security.LoginAttemptProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        service = new LoginAttemptService(redisTemplate, new LoginAttemptProperties(5, Duration.ofMinutes(15)));
    }

    @Test
    void reportsLockedAtThreshold() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("auth:login-attempts:user@example.com")).thenReturn("5");

        assertThat(service.isLocked("user@example.com")).isTrue();
    }

    @Test
    void recordsFailureAtomically() {
        when(redisTemplate.execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                eq(List.of("auth:login-attempts:user@example.com")),
                eq("900")
        ))
                .thenReturn(3L);

        assertThat(service.recordFailure("user@example.com")).isEqualTo(3);
    }

    @Test
    void rejectsMissingRedisResult() {
        when(redisTemplate.execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                org.mockito.ArgumentMatchers.<String>anyList(),
                any(String.class)
        )).thenReturn(null);

        assertThatThrownBy(() -> service.recordFailure("user@example.com"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void resetDeletesCounterAndExposesThreshold() {
        service.reset("user@example.com");

        verify(redisTemplate).delete("auth:login-attempts:user@example.com");
        assertThat(service.maxAttempts()).isEqualTo(5);
    }
}
