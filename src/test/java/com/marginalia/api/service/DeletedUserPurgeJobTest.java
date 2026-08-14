package com.marginalia.api.service;

import com.marginalia.api.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DeletedUserPurgeJobTest {

    @Test
    void purgesUsersDeletedAtLeastThirtyDaysAgo() {
        UserRepository repository = mock(UserRepository.class);
        DeletedUserPurgeJob job = new DeletedUserPurgeJob(repository);
        Instant expected = Instant.now().minus(Duration.ofDays(30));

        job.purgeDeletedUsers();

        verify(repository).deleteAllByDeletedAtBefore(argThat(cutoff ->
                Duration.between(cutoff, expected).abs().compareTo(Duration.ofSeconds(2)) < 0));
    }
}
