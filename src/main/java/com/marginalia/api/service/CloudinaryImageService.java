package com.marginalia.api.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.marginalia.api.dto.CloudinaryUploadResult;
import com.marginalia.api.exception.CloudinaryUploadException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
        } catch (IOException exception) {
            throw new CloudinaryUploadException(exception);
        }
    }

    private CloudinaryUploadResult toResult(Map<?, ?> uploadResult) {
        Object secureUrl = uploadResult.get("secure_url");
        Object bytes = uploadResult.get("bytes");
        if (!(secureUrl instanceof String url) || url.isBlank() || !(bytes instanceof Number size)) {
            throw new CloudinaryUploadException();
        }
        return new CloudinaryUploadResult(url, size.longValue());
    }
}
