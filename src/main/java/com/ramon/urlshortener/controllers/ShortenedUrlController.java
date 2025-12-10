package com.ramon.urlshortener.controllers;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ramon.urlshortener.domain.dtos.ShortenedUrlDTO;
import com.ramon.urlshortener.services.ShortenedUrlService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShortenedUrlController {

    private final ShortenedUrlService urlShortenerService;

    @PostMapping("/shorten-url")
    public ResponseEntity<ShortenedUrlDTO> shortenUrl(@RequestBody @Valid ShortenedUrlDTO dto) {
        ShortenedUrlDTO saved = urlShortenerService.save(dto);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{shortening}")
    public ResponseEntity<Void> redirect(@PathVariable String shortening) {
        ShortenedUrlDTO shortenedUrl = urlShortenerService.findByShortening(shortening);
        HttpHeaders headers = new HttpHeaders();
        URI location = URI.create(shortenedUrl.getUrl());
        headers.setLocation(location);
        return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).build();
    }
    
}
