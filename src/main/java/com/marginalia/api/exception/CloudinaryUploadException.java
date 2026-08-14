package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Indicates that Cloudinary failed to accept an image or returned invalid upload metadata. */
@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class CloudinaryUploadException extends RuntimeException {

    /** Creates the exception without an underlying cause. */
    public CloudinaryUploadException() {
        super("Image could not be uploaded");
    }

    /**
     * Creates the exception for an underlying upload failure.
     *
     * @param cause original upload failure
     */
    public CloudinaryUploadException(Throwable cause) {
        super("Image could not be uploaded", cause);
    }
}
