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
import static org.mockito.Mockito.verify;

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
                .thenReturn(Map.of(
                        "secure_url", "https://example.test/image.png",
                        "bytes", 2L,
                        "public_id", "marginalia/attachments/image"
                ));

        var result = service.upload(file);

        assertThat(result.secureUrl()).isEqualTo("https://example.test/image.png");
        assertThat(result.sizeBytes()).isEqualTo(2);
        assertThat(result.publicId()).isEqualTo("marginalia/attachments/image");
    }

    @Test
    void deletesStoredCloudinaryAsset() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(any(String.class), any(Map.class))).thenReturn(Map.of("result", "ok"));

        service.delete("marginalia/attachments/image", "https://example.test/image.png");

        verify(uploader).destroy(org.mockito.ArgumentMatchers.eq("marginalia/attachments/image"), any(Map.class));
    }

    @Test
    void resolvesLegacyPublicIdFromCloudinaryUrl() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(any(String.class), any(Map.class))).thenReturn(Map.of("result", "ok"));

        service.delete(null, "https://res.cloudinary.com/demo/image/upload/v123/marginalia/attachments/chart_ab12.png");

        verify(uploader).destroy(org.mockito.ArgumentMatchers.eq("marginalia/attachments/chart_ab12"), any(Map.class));
    }

    @Test
    void wrapsUploadIoFailure() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class))).thenThrow(new IOException("network"));

        assertThatThrownBy(() -> service.upload(new MockMultipartFile("file", new byte[]{1})))
                .isInstanceOf(CloudinaryUploadException.class);
    }

    @Test
    void wrapsCloudinaryRuntimeFailure() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenThrow(new RuntimeException("Invalid cloud_name"));

        assertThatThrownBy(() -> service.upload(new MockMultipartFile("file", new byte[]{1})))
                .isInstanceOf(CloudinaryUploadException.class)
                .hasMessage("Image could not be uploaded");
    }

    @Test
    void rejectsIncompleteUploadResponse() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(Map.of());

        assertThatThrownBy(() -> service.upload(new MockMultipartFile("file", new byte[]{1})))
                .isInstanceOf(CloudinaryUploadException.class);
    }
}
