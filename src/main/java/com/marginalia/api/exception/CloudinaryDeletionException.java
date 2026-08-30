package com.marginalia.api.exception;

public class CloudinaryDeletionException extends RuntimeException {

    public CloudinaryDeletionException() {
        super("Image could not be deleted");
    }

    public CloudinaryDeletionException(Throwable cause) {
        super("Image could not be deleted", cause);
    }
}
