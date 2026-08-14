package com.marginalia.api.service;

import com.marginalia.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/** Permanently removes accounts whose soft-deletion retention period has elapsed. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeletedUserPurgeJob {

    private static final Duration RETENTION_PERIOD = Duration.ofDays(30);

    private final UserRepository userRepository;

    /** Purges users soft-deleted more than thirty days ago using database cascades for owned data. */
    @Scheduled(cron = "${account-deletion.purge-cron:0 0 3 * * *}", zone = "UTC")
    @Transactional
    public void purgeDeletedUsers() {
        int purgedUsers = userRepository.deleteAllByDeletedAtBefore(
                Instant.now().minus(RETENTION_PERIOD)
        );
        if (purgedUsers > 0) {
            log.info("Permanently purged {} soft-deleted users", purgedUsers);
        }
    }
}
