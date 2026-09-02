package com.snippet.common.exception;

import com.snippet.common.api.CommonResult;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonResult<Void>> handleBusinessException(BusinessException e) {
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(CommonResult.failed(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResult<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(this::validationMessage)
                .orElse("请求参数不合法");
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CommonResult<Void>> handleConstraintViolation(
            ConstraintViolationException e) {
        String message = e.getConstraintViolations()
                .stream()
                .findFirst()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .orElse("请求参数不合法");
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CommonResult<Void>> handleUnreadableRequest(
            HttpMessageNotReadableException e) {
        return error(HttpStatus.BAD_REQUEST, "请求体格式错误");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<CommonResult<Void>> handleDuplicateKey(DuplicateKeyException e) {
        return error(HttpStatus.CONFLICT, "数据已存在");
    }

    private ResponseEntity<CommonResult<Void>> error(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(CommonResult.failed(status.value(), message));
    }

    private String validationMessage(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }
}
