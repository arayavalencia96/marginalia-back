package com.marginalia.api.service;

import com.marginalia.api.config.CloudinaryProperties;
import com.marginalia.api.domain.Attachment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Component
public class CloudinaryImageDownloader {

    private static final long MAX_INLINE_IMAGE_SIZE = 1_048_576;
    private static final String CLOUDINARY_HOST = "res.cloudinary.com";

    private final String cloudPathPrefix;
    private final HttpClient httpClient;

    public CloudinaryImageDownloader(CloudinaryProperties properties) {
        this.cloudPathPrefix = "/" + properties.cloudName() + "/";
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    public Optional<byte[]> downloadForEmbedding(Attachment attachment) {
        if (attachment.getSizeBytes() > MAX_INLINE_IMAGE_SIZE) {
            return Optional.empty();
        }

        try {
            URI uri = URI.create(attachment.getUrl());
            if (!isAllowedCloudinaryUrl(uri)) {
                return Optional.empty();
            }

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(5))
                    .header("Accept", "image/*")
                    .GET()
                    .build();
            HttpResponse<InputStream> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofInputStream()
            );
            if (response.statusCode() != 200 || !isImage(response)) {
                response.body().close();
                return Optional.empty();
            }

            try (InputStream body = response.body()) {
                byte[] bytes = body.readNBytes((int) MAX_INLINE_IMAGE_SIZE + 1);
                return bytes.length <= MAX_INLINE_IMAGE_SIZE
                        ? Optional.of(bytes)
                        : Optional.empty();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (Exception exception) {
            log.debug("Could not embed Cloudinary image {}", attachment.getId(), exception);
            return Optional.empty();
        }
    }

    private boolean isAllowedCloudinaryUrl(URI uri) {
        return "https".equalsIgnoreCase(uri.getScheme())
                && CLOUDINARY_HOST.equalsIgnoreCase(uri.getHost())
                && uri.getPath() != null
                && uri.getPath().startsWith(cloudPathPrefix);
    }

    private boolean isImage(HttpResponse<?> response) {
        return response.headers().firstValue("Content-Type")
                .map(value -> value.toLowerCase(Locale.ROOT).startsWith("image/"))
                .orElse(false);
    }
}
