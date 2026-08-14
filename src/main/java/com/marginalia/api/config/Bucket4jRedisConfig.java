package com.marginalia.api.config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.time.Duration;

/** Configures the Redis connection and distributed Bucket4j proxy manager used for rate limiting. */
@Configuration
public class Bucket4jRedisConfig {

    @Bean(destroyMethod = "shutdown")
    RedisClient bucket4jRedisClient(RedisProperties properties) {
        return RedisClient.create(redisUri(properties));
    }

    @Bean(destroyMethod = "close")
    StatefulRedisConnection<String, byte[]> bucket4jRedisConnection(RedisClient bucket4jRedisClient) {
        RedisCodec<String, byte[]> codec = RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE);
        return bucket4jRedisClient.connect(codec);
    }

    @Bean
    ProxyManager<String> authRateLimitProxyManager(
            StatefulRedisConnection<String, byte[]> bucket4jRedisConnection
    ) {
        return Bucket4jLettuce.casBasedBuilder(bucket4jRedisConnection)
                .expirationAfterWrite(ExpirationAfterWriteStrategy
                        .basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(1)))
                .build();
    }

    private RedisURI redisUri(RedisProperties properties) {
        if (StringUtils.hasText(properties.getUrl())) {
            return RedisURI.create(properties.getUrl());
        }

        RedisURI.Builder builder = RedisURI.Builder.redis(properties.getHost(), properties.getPort())
                .withDatabase(properties.getDatabase())
                .withSsl(properties.getSsl().isEnabled());

        if (properties.getTimeout() != null) {
            builder.withTimeout(properties.getTimeout());
        }
        if (StringUtils.hasText(properties.getUsername()) && StringUtils.hasText(properties.getPassword())) {
            builder.withAuthentication(properties.getUsername(), properties.getPassword());
        } else if (StringUtils.hasText(properties.getPassword())) {
            builder.withPassword((CharSequence) properties.getPassword());
        }

        return builder.build();
    }
}
