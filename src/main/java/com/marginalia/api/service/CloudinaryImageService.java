package com.marginalia.api.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.marginalia.api.dto.CloudinaryUploadResult;
import com.marginalia.api.exception.CloudinaryDeletionException;
import com.marginalia.api.exception.CloudinaryUploadException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Uploads validated image data to the application's Cloudinary attachment folder. */
@Service
@RequiredArgsConstructor
public class CloudinaryImageService {

    private static final String ATTACHMENTS_FOLDER = "marginalia/attachments";

    private final Cloudinary cloudinary;

    /**
     * Uploads an image to Cloudinary without overwriting existing assets.
     *
     * @param file image file to upload
     * @return secure URL and byte size returned by Cloudinary
     * @throws CloudinaryUploadException if the upload fails or returns incomplete metadata
     */
    public CloudinaryUploadResult upload(MultipartFile file) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "folder", ATTACHMENTS_FOLDER,
                            "unique_filename", true,
                            "overwrite", false
                    )
            );
            return toResult(uploadResult);
        } catch (CloudinaryUploadException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw new CloudinaryUploadException(exception);
        }
    }

    public void delete(String storedPublicId, String secureUrl) {
        String publicId = storedPublicId == null || storedPublicId.isBlank()
                ? publicIdFromUrl(secureUrl)
                : storedPublicId;
        try {
            Map<?, ?> result = cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("resource_type", "image", "invalidate", true)
            );
            Object status = result.get("result");
            if (!"ok".equals(status) && !"not found".equals(status)) {
                throw new CloudinaryDeletionException();
            }
        } catch (CloudinaryDeletionException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw new CloudinaryDeletionException(exception);
        }
    }

    private CloudinaryUploadResult toResult(Map<?, ?> uploadResult) {
        Object secureUrl = uploadResult.get("secure_url");
        Object bytes = uploadResult.get("bytes");
        Object publicId = uploadResult.get("public_id");
        if (!(secureUrl instanceof String url) || url.isBlank()
                || !(bytes instanceof Number size)
                || !(publicId instanceof String assetId) || assetId.isBlank()) {
            throw new CloudinaryUploadException();
        }
        return new CloudinaryUploadResult(url, size.longValue(), assetId);
    }

    private String publicIdFromUrl(String secureUrl) {
        try {
            String path = URLDecoder.decode(URI.create(secureUrl).getPath(), StandardCharsets.UTF_8);
            String folderPrefix = ATTACHMENTS_FOLDER + "/";
            int folderIndex = path.indexOf(folderPrefix);
            if (folderIndex < 0) {
                throw new CloudinaryDeletionException();
            }
            String publicIdWithExtension = path.substring(folderIndex);
            int extensionIndex = publicIdWithExtension.lastIndexOf('.');
            if (extensionIndex <= publicIdWithExtension.lastIndexOf('/')) {
                throw new CloudinaryDeletionException();
            }
            return publicIdWithExtension.substring(0, extensionIndex);
        } catch (CloudinaryDeletionException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new CloudinaryDeletionException(exception);
        }
    }
}
