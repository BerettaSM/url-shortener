package com.ramon.urlshortener.domain.dtos;

import java.time.Instant;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@Data
@EqualsAndHashCode
public class CustomError {

    private String message;
    private Integer status;
    private Instant timestamp;
    private String path;

    public CustomError(String message, HttpStatus status) {
        this.message = message;
        this.status = status.value();
        timestamp = Instant.now();
        path = Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .map(req -> ((ServletRequestAttributes) req))
                .map(ServletRequestAttributes::getRequest)
                .map(HttpServletRequest::getRequestURI)
                .orElse("Unknown path");
    }

}
