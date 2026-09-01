package com.snippet.common.exception;


import com.snippet.common.api.IErrorCode;

/**
 * 自定义Api异常
 * 写入26年8月29日
 */
public class ApiException extends RuntimeException {
    private IErrorCode errorCode;

    public ApiException(IErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(Throwable cause) {
        super(cause);
    }
    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
    public IErrorCode getErrorCode() {
        return errorCode;
    }
}
