package com.snippet.common.exception;

import com.snippet.common.api.CommonResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public CommonResult<Void> handle(BusinessException e) {
        return CommonResult.failed(
                e.getCode(),
                e.getMessage()
        );

    }
}
