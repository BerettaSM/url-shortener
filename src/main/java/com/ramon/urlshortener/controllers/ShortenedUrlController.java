package com.ramon.urlshortener.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ramon.urlshortener.domain.dtos.ShortenedUrlDTO;
import com.ramon.urlshortener.services.ShortenedUrlService;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShortenedUrlController {

    private final ShortenedUrlService urlShortenerService;

    @PostMapping("/shorten-url")
    public ResponseEntity<ShortenedUrlDTO> shortenUrl(@RequestBody ShortenedUrlDTO dto) {
        ShortenedUrlDTO saved = urlShortenerService.save(dto);
        return ResponseEntity.ok(saved);
    }

}
