package com.ramon.urlshortener.repositories;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.ramon.urlshortener.domain.entities.ShortenedUrl;
import com.ramon.urlshortener.exceptions.DatabaseException;

@Repository
public class RedisShortenedUrlRepository implements ShortenedUrlRepository {

    private static final String URL_KEY_PREFIX = "urls";

    private final StringRedisTemplate redisTemplate;
    private final HashOperations<String, String, String> hashOperations;

    public RedisShortenedUrlRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public Optional<ShortenedUrl> findById(String shortenedUrlId) {
        String urlKey = getKey(shortenedUrlId);
        Map<String, String> map = hashOperations.entries(urlKey);
        return Optional.of(map)
                .filter(m -> !m.isEmpty())
                .map(RedisShortenedUrlRepository::toShortenedUrl);
    }

    @Override
    public List<ShortenedUrl> findAll() {
        @SuppressWarnings("Convert2Lambda")
        List<Map<String, String>> maps = redisTemplate.execute(new SessionCallback<List<Map<String, String>>>() {
            @SuppressWarnings({ "rawtypes", "null", "unchecked" })
            @Override
            public List<Map<String, String>> execute(RedisOperations operations) throws DataAccessException {
                Set<String> keys = operations.keys(URL_KEY_PREFIX + ":*");
                operations.multi();
                keys.forEach(urlKey -> operations.opsForHash().entries(urlKey));
                return operations.exec();
            }

        });
        return maps.stream().map(RedisShortenedUrlRepository::toShortenedUrl).toList();
    }

    @Override
    public void save(ShortenedUrl shortenedUrl) {
        String urlKey = getKey(shortenedUrl);
        Map<String, String> transactionMap = toMap(shortenedUrl);
        hashOperations.putAll(urlKey, transactionMap);
    }

    @Override
    public void deleteById(String shortenedUrlId) {
        String urlKey = getKey(shortenedUrlId);
        hashOperations.delete(urlKey, "id", "url", "expiryMoment");
    }

    private static @NonNull ShortenedUrl toShortenedUrl(Map<String, String> map) {
        try {
            String id = map.get("id");
            String url = map.get("url");
            Instant expiryMoment = Instant.parse(map.get("expiryMoment"));
            return new ShortenedUrl(id, url, expiryMoment);
        } catch (DateTimeParseException e) {
            throw new DatabaseException("Unexpected ShortenedUrl storage format");
        }
    }

    private static @NonNull Map<String, String> toMap(ShortenedUrl shortenedUrl) {
        Map<String, String> transactionMap = new HashMap<>();
        transactionMap.put("id", shortenedUrl.getId());
        transactionMap.put("url", shortenedUrl.getUrl());
        transactionMap.put("expiryMoment", shortenedUrl.getExpiryMoment().toString());
        return transactionMap;
    }

    private static @NonNull String getKey(ShortenedUrl shortenedUrl) {
        return getKey(shortenedUrl.getId());
    }

    private static @NonNull String getKey(String shortenedUrlId) {
        return URL_KEY_PREFIX + ":" + shortenedUrlId;
    }

}
