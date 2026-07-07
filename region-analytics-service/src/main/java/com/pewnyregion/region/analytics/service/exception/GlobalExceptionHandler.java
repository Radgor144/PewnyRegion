package com.pewnyregion.region.analytics.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ProblemDetail handleWebExchangeBindException(WebExchangeBindException ex) {
        String details = ex.getBindingResult()
                           .getAllErrors()
                           .stream()
                           .map(error -> error instanceof FieldError fe
                                   ? fe.getField() + ": " + fe.getDefaultMessage()
                                   : error.getDefaultMessage())
                           .collect(Collectors.joining(", "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, details);
    }

    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApiException(ApiException ex) {
        return ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getMessage());
    }

    @ExceptionHandler(HttpStatusCodeException.class)
    public ProblemDetail handleHttpStatusCodeException(HttpStatusCodeException ex) {
        return ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleRuntimeException(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
}