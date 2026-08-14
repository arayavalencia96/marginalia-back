package com.marginalia.api.exception;

import com.marginalia.api.controller.AuthController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthExceptionHandler {

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ProblemDetail handleEmailNotVerified(EmailNotVerifiedException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(LoginLockedException.class)
    public ProblemDetail handleLoginLocked(LoginLockedException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.LOCKED, exception.getMessage());
    }
}
