package com.ramon.urlshortener.repositories;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import com.ramon.urlshortener.domain.entities.ShortenedUrl;
import com.ramon.urlshortener.exceptions.DatabaseException;
import com.ramon.urlshortener.services.ShorteningService;

@Repository
public class RedisShortenedUrlRepository implements ShortenedUrlRepository {

    private static final String URL_KEY_PREFIX = "urls";

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Boolean> setHashIfNotExistsScript;
    private final HashOperations<String, String, String> hashOperations;

    private final ShorteningService shorteningService;

    public RedisShortenedUrlRepository(
            StringRedisTemplate redisTemplate,
            ShorteningService shorteningService,
            RedisScript<Boolean> setHashIfNotExistsScript) {
        this.redisTemplate = redisTemplate;
        this.shorteningService = shorteningService;
        this.setHashIfNotExistsScript = setHashIfNotExistsScript;
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
    public ShortenedUrl save(ShortenedUrl shortenedUrl) {
        String shortening, args[];
        ShortenedUrl saveCandidate = copy(shortenedUrl);
        do {
            shortening = shorteningService.generateShortening();
            saveCandidate.setId(shortening);
            args = toVarArgs(saveCandidate);
        } while (!saveShorteningIfIdNotExists(getKey(shortening), args));
        return saveCandidate;
    }

    @Override
    public void deleteById(String shortenedUrlId) {
        String urlKey = getKey(shortenedUrlId);
        hashOperations.delete(urlKey, "id", "url", "expiryMoment");
    }

    private boolean saveShorteningIfIdNotExists(String id, String[] args) {
        return redisTemplate.<Boolean>execute(setHashIfNotExistsScript, List.of(id), (Object[]) args);
    }

    private static String[] toVarArgs(ShortenedUrl shortenedUrl) {
        return new String[] {
                "id", shortenedUrl.getId(),
                "url", shortenedUrl.getUrl(),
                "expiryMoment", shortenedUrl.getExpiryMoment().toString()
        };
    }

    private static ShortenedUrl toShortenedUrl(Map<String, String> map) {
        try {
            String id = map.get("id");
            String url = map.get("url");
            Instant expiryMoment = Instant.parse(map.get("expiryMoment"));
            return new ShortenedUrl(id, url, expiryMoment);
        } catch (DateTimeParseException e) {
            throw new DatabaseException("Unexpected ShortenedUrl storage format");
        }
    }

    private static ShortenedUrl copy(ShortenedUrl target) {
        ShortenedUrl shortenedUrl = new ShortenedUrl();
        shortenedUrl.setId(target.getId());
        shortenedUrl.setUrl(target.getUrl());
        shortenedUrl.setExpiryMoment(target.getExpiryMoment());
        return shortenedUrl;
    }

    private static String getKey(String shortenedUrlId) {
        return URL_KEY_PREFIX + ":" + shortenedUrlId;
    }

}
