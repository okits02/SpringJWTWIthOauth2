package com.okits02.SpringJWTWithOauth2.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTS(1002, "User already exists", HttpStatus.CONFLICT),
    USER_NOT_FOUND(1003, "User not found", HttpStatus.NOT_FOUND),
    INVALID_PASSWORD(
            1004,
            "Password must have at least one uppercase characters and at lest {min} character",
            HttpStatus.UNPROCESSABLE_ENTITY),
    INVALID_PHONE(1006, "Phone is not valid", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1007, "Email is not valid", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME(1008, "Username must have at lest {min} character", HttpStatus.BAD_REQUEST),
    ROLE_EXISTS(1009, "Role already exists", HttpStatus.CONFLICT),
    ROLE_NOT_FOUND(1010, "Role not found", HttpStatus.NOT_FOUND),
    PERMISSION_EXISTS(1012, "Permission already exists", HttpStatus.CONFLICT),
    PERMISSION_NOT_FOUND(1011, "Permission not found", HttpStatus.NOT_FOUND),
    TOKEN_NOT_FOUND(1012, "Can't find token", HttpStatus.NOT_FOUND),
    REFRESH_TOKEN_NOT_FOUND(1013, "Can't find refresh token", HttpStatus.NOT_FOUND),
    OAUTH2_TOKEN_EXCHANGE_FAILED(1014, "Failed to exchange Google authorization code", HttpStatus.BAD_REQUEST),
    OAUTH2_USER_INFO_FAILED(1015, "Failed to fetch Google user info", HttpStatus.BAD_REQUEST),
    OAUTH2_INVALID_USER_INFO(1016, "Google user info is invalid", HttpStatus.BAD_REQUEST),
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
