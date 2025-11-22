package com.ramon.urlshortener.repositories;

import java.util.List;
import java.util.Optional;

import com.ramon.urlshortener.domain.entities.ShortenedUrl;

public interface ShortenedUrlRepository {

    Optional<ShortenedUrl> findById(String shortenedUrlId);

    List<ShortenedUrl> findAll();

    void save(ShortenedUrl shortenedUrl);

    void deleteById(String shortenedUrlId);
    
}
