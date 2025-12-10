package com.ramon.urlshortener.controllers.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.ramon.urlshortener.domain.dtos.CustomError;
import com.ramon.urlshortener.domain.dtos.ValidationError;
import com.ramon.urlshortener.exceptions.ResourceNotFoundException;

@ControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CustomError> resourceNotFound(ResourceNotFoundException e) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        CustomError err = new CustomError(e.getMessage(), status);
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> validationException(MethodArgumentNotValidException e) {
        ValidationError err = ValidationError.from(e);
        return ResponseEntity.status(err.getStatus()).body(err);
    }

}
