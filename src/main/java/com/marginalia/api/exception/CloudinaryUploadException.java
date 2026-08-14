package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class CloudinaryUploadException extends RuntimeException {

    public CloudinaryUploadException() {
        super("Image could not be uploaded");
    }

    public CloudinaryUploadException(Throwable cause) {
        super("Image could not be uploaded", cause);
    }
}
