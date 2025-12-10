package com.ramon.urlshortener.tasks;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ramon.urlshortener.domain.entities.ShortenedUrl;
import com.ramon.urlshortener.repositories.ShortenedUrlRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Log4j2
public class CleanupTask {

    private final ShortenedUrlRepository shortenedUrlRepository;

    @Scheduled(fixedRateString = "${custom.url.expiry-in-seconds}", timeUnit = TimeUnit.SECONDS)
    public void performCleanup() {
        List<String> expiredIds = shortenedUrlRepository.findAll()
                .stream()
                .filter(ShortenedUrl::isExpired)
                .map(ShortenedUrl::getId)
                .toList();
        log.info("Cleaning up {} expired urls.", expiredIds.size());
        shortenedUrlRepository.deleteAllById(expiredIds);
    }

}
