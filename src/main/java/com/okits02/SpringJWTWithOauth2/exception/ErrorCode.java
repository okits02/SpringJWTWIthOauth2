package com.okits02.SpringJWTWithOauth2.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTS(1002, "User already exists", HttpStatus.CONFLICT),
    USER_NOT_FOUND(1003, "User not found", HttpStatus.NOT_FOUND),
    ;
    private final int code;
    private final String message;
    private final HttpStatusCode status;

    ErrorCode(int code, String message, HttpStatusCode status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
