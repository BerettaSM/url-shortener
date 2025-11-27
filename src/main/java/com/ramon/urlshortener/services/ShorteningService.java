package com.ramon.urlshortener.services;

import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShorteningService {

    private static final int MIN_CHARACTERS = 5;
    private static final int MAX_CHARACTERS = 10;

    private static final Random random = new Random();
    
    public String generateShortening() {
        UUID uuid = UUID.randomUUID();
        int totalChars = random.nextInt(MIN_CHARACTERS, MAX_CHARACTERS + 1);
        return uuid.toString().replace("-", "").substring(0, totalChars).toUpperCase();
    }

}
