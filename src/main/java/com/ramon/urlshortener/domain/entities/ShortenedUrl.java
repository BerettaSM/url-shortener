package com.ramon.urlshortener.domain.entities;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ShortenedUrl {

    private String id;
    private String url;
    private Instant expiryMoment;

    public boolean isExpired() {
        return expiryMoment.isBefore(Instant.now());
    }

}
