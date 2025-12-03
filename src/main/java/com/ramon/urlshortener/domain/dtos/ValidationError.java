package com.ramon.urlshortener.domain.dtos;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@Data
@EqualsAndHashCode
public class ValidationError {

    private String message;
    private Integer status;
    private Instant timestamp;
    private String path;

    public static record Field(String fieldName, Set<String> messages) {

        public Field {
            messages = new HashSet<>(messages);
        }

    }

    private final List<Field> errors = new ArrayList<>();

    public ValidationError() {
        message = "Validation error(s)";
        status = HttpStatus.UNPROCESSABLE_ENTITY.value();
        timestamp = Instant.now();
        path = Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .map(req -> ((ServletRequestAttributes) req))
                .map(ServletRequestAttributes::getRequest)
                .map(HttpServletRequest::getRequestURI)
                .orElse("Unknown path");
    }

    public ValidationError(MethodArgumentNotValidException e) {
        this();
        errors.addAll(extractErrors(e));
    }

    public static ValidationError from(MethodArgumentNotValidException e) {
        return new ValidationError(e);
    }

    public ValidationError addError(String fieldName, String message) {
        errors.stream()
                .filter(e -> fieldName.equals(e.fieldName()))
                .findFirst()
                .ifPresentOrElse(
                        e -> e.messages().add(message),
                        () -> errors.add(new Field(fieldName, Set.of(message))));
        return this;
    }

    private static Set<Field> extractErrors(MethodArgumentNotValidException e) {
        return e.getFieldErrors()
                .stream()
                .collect(
                        Collectors.groupingBy(
                                FieldError::getField,
                                Collectors.mapping(
                                        FieldError::getDefaultMessage,
                                        Collectors.toSet())))
                .entrySet()
                .stream()
                .map(entry -> new Field(entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());
    }

}
