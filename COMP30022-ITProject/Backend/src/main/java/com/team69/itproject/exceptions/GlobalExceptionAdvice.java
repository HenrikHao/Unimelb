package com.team69.itproject.exceptions;

import com.alibaba.fastjson.JSON;
import com.team69.itproject.entities.bo.ResponseEntity;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionAdvice {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleBusinessException(BusinessException e) {
        String message = "Error code: " + e.getCode() + " Details: " + e.getLocalizedMessage();
        log.error(message);
        return ResponseEntity.error(e.getCode(), message);
    }


    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleNotFoundException(NotFoundException e) {
        log.error("Not Found: {}", e.getLocalizedMessage());
        String message = e.getMessage();
        return ResponseEntity.error(404, message);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException e) {
        log.error("Access Denied: {}", e.getLocalizedMessage());
        String message = e.getMessage();
        return ResponseEntity.error(403, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("Bad request: {}", e.getLocalizedMessage());
        String message = e.getMessage();
        return ResponseEntity.error(400, message);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleBadRequestException(BadRequestException e) {
        log.error("Bad request: {}", e.getLocalizedMessage());
        String message = e.getMessage();
        return ResponseEntity.error(400, message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleException(Exception e) {
        log.error(e.getLocalizedMessage());
        String message = e.getMessage();
        return ResponseEntity.error(500, message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleBodyValidException(MethodArgumentNotValidException exception) {
        Map<String, String> errorMsg = new HashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errorMsg.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        String jsonMsg = JSON.toJSONString(errorMsg);
        log.error("Invalid Parameter: {}", jsonMsg);
        return ResponseEntity.error(400, errorMsg);
    }
}
