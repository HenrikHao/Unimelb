package com.team69.itproject.handlers;

import com.team69.itproject.entities.bo.ResponseEntity;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Objects;

@Log4j2
@RestControllerAdvice
public class ResponseHandler implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body == null) {
            return null;
        }
        log.info("Response out: class = {} content = {}", body.getClass().getSimpleName(), body);
        if (body.getClass().getPackage().getName().contains("com.team69.itproject")) {
            if (body instanceof ResponseEntity<?> responseEntity) {
                HttpStatus httpStatus = HttpStatus.resolve(responseEntity.getCode());
                response.setStatusCode(Objects.requireNonNullElse(httpStatus, HttpStatus.OK));
                return body;
            }
            return ResponseEntity.ok(body);
        } else {
            return body;
        }

    }
}
