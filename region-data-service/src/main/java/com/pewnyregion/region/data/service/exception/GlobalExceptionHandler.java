package com.pewnyregion.region.data.service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ProblemDetail handleWebExchangeBindException(WebExchangeBindException ex, ServerWebExchange exchange) {
        String details = ex.getBindingResult()
                           .getAllErrors()
                           .stream()
                           .map(error -> error instanceof FieldError fe
                                   ? fe.getField() + ": " + fe.getDefaultMessage()
                                   : error.getDefaultMessage())
                           .collect(Collectors.joining(", "));
        logClientError(exchange, ex, HttpStatus.BAD_REQUEST.value(), details);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, details);
    }

    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApiException(ApiException ex, ServerWebExchange exchange) {
        logClientError(exchange, ex, ex.getStatusCode().value(), ex.getMessage());
        return ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatusException(ResponseStatusException ex, ServerWebExchange exchange) {
        String detail = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        logClientError(exchange, ex, ex.getStatusCode().value(), detail);
        return ProblemDetail.forStatusAndDetail(ex.getStatusCode(), detail);
    }

    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleRuntimeException(RuntimeException ex, ServerWebExchange exchange) {
        logServerError(exchange, ex, HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private void logClientError(ServerWebExchange exchange, Exception ex, int status, String detail) {
        log.warn("Client error handling request path=\"{}\" method=\"{}\" status={} exception=\"{}\" detail=\"{}\"",
                exchange.getRequest().getPath().value(),
                exchange.getRequest().getMethod().name(),
                status,
                ex.getClass().getSimpleName(),
                detail);
    }

    private void logServerError(ServerWebExchange exchange, Exception ex, int status, String detail) {
        log.error("Server error handling request path=\"{}\" method=\"{}\" status={} exception=\"{}\" detail=\"{}\"",
                exchange.getRequest().getPath().value(),
                exchange.getRequest().getMethod().name(),
                status,
                ex.getClass().getSimpleName(),
                detail,
                ex);
    }
}
