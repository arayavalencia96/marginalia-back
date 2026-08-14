package com.marginalia.api.service;

import com.marginalia.api.config.CloudinaryProperties;
import com.marginalia.api.domain.Attachment;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CloudinaryImageDownloaderTest {

    private final CloudinaryImageDownloader downloader =
            new CloudinaryImageDownloader(new CloudinaryProperties("my-cloud", "key", "secret"));

    @Test
    void skipsOversizedImage() {
        Attachment attachment = Attachment.builder().sizeBytes(1_048_577).url("https://res.cloudinary.com/my-cloud/image.png").build();

        assertThat(downloader.downloadForEmbedding(attachment)).isEmpty();
    }

    @Test
    void rejectsNonCloudinaryUrlWithoutNetworkCall() {
        Attachment attachment = Attachment.builder().sizeBytes(100).url("https://example.com/image.png").build();

        assertThat(downloader.downloadForEmbedding(attachment)).isEmpty();
    }
}
