package com.marginalia.api.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.marginalia.api.exception.CloudinaryUploadException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloudinaryImageServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryImageService service;

    @Test
    void uploadsImageAndMapsMetadata() throws IOException {
        var file = new MockMultipartFile("file", "image.png", "image/png", new byte[]{1, 2});
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenReturn(Map.of("secure_url", "https://example.test/image.png", "bytes", 2L));

        var result = service.upload(file);

        assertThat(result.secureUrl()).isEqualTo("https://example.test/image.png");
        assertThat(result.sizeBytes()).isEqualTo(2);
    }

    @Test
    void wrapsUploadIoFailure() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class))).thenThrow(new IOException("network"));

        assertThatThrownBy(() -> service.upload(new MockMultipartFile("file", new byte[]{1})))
                .isInstanceOf(CloudinaryUploadException.class);
    }

    @Test
    void rejectsIncompleteUploadResponse() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(Map.of());

        assertThatThrownBy(() -> service.upload(new MockMultipartFile("file", new byte[]{1})))
                .isInstanceOf(CloudinaryUploadException.class);
    }
}
