package com.marginalia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ChapterNotFoundException extends RuntimeException {

    public ChapterNotFoundException(UUID id) {
        super("Chapter not found: " + id);
    }
}
