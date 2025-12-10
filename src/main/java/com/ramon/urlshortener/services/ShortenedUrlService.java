package com.ramon.urlshortener.services;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ramon.urlshortener.domain.dtos.ShortenedUrlDTO;
import com.ramon.urlshortener.domain.entities.ShortenedUrl;
import com.ramon.urlshortener.repositories.ShortenedUrlRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShortenedUrlService {

    @Value("${custom.url.expiry-in-seconds}")
    private Integer expiryInSeconds;

    private final ShortenedUrlRepository shortenedUrlRepository;

    public ShortenedUrlDTO findByShortening(String shortening) {
        return shortenedUrlRepository.findById(shortening)
                .map(su -> new ShortenedUrlDTO(su.getUrl()))
                .orElseThrow(() -> new RuntimeException("Url not found"));
    }

    public ShortenedUrlDTO save(ShortenedUrlDTO dto) {
        ShortenedUrl shortenedUrl = new ShortenedUrl();
        shortenedUrl.setUrl(dto.getUrl());
        shortenedUrl.setExpiryMoment(Instant.now().plusSeconds(expiryInSeconds));
        shortenedUrl = shortenedUrlRepository.save(shortenedUrl);
        String url = getShortenedUrl(shortenedUrl.getId());
        return new ShortenedUrlDTO(url);
    }

    private static String getShortenedUrl(String shortening) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(shortening)
                .build()
                .toUriString();
    }

}
