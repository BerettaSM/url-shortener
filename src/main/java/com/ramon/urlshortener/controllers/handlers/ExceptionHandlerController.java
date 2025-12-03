package com.ramon.urlshortener.controllers.handlers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.ramon.urlshortener.domain.dtos.ValidationError;

@ControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> validationException(MethodArgumentNotValidException e) {
        ValidationError err = ValidationError.from(e);
        return ResponseEntity.status(err.getStatus()).body(err);
    }

}
