package com.pewnyregion.region.analytics.service.exception;

import org.springframework.http.HttpStatus;

public class MapValidationException extends ApiException {

    public MapValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
