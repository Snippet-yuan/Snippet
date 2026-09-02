package com.snippet.common.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final int code;

    public BusinessException(HttpStatus httpStatus, int code, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
    }

    public BusinessException(HttpStatus httpStatus, String message) {
        this(httpStatus, httpStatus.value(), message);
    }

    public BusinessException(int code, String message) {
        this(resolveHttpStatus(code), code, message);
    }

    public BusinessException(String message) {
        this(HttpStatus.BAD_REQUEST, message);
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public int getCode() {
        return code;
    }

    private static HttpStatus resolveHttpStatus(int code) {
        HttpStatus status = HttpStatus.resolve(code);
        return status == null ? HttpStatus.BAD_REQUEST : status;
    }
}
