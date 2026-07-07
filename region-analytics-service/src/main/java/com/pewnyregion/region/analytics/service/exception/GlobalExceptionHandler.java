package com.pewnyregion.region.analytics.service.exception;

import com.pewnyregion.region.analytics.service.model.exception.ApiException;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApiException(ApiException ex) {
        return ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getMessage());
    }

    @ExceptionHandler(HttpStatusCodeException.class)
    public ProblemDetail handleHttpStatusCodeException(HttpStatusCodeException ex) {
        return ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getMessage());
    }
}