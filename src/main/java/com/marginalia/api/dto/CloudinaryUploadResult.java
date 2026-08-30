package com.marginalia.api.dto;

/**
 * Contains metadata returned after a Cloudinary image upload.
 *
 * @param secureUrl secure URL of the uploaded image
 * @param sizeBytes uploaded image size in bytes
 * @param publicId Cloudinary asset identifier
 */
public record CloudinaryUploadResult(
        String secureUrl,
        long sizeBytes,
        String publicId
) {
}
