package com.example.websocket_demo.service.redis;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Thin abstraction over the shared {@code StringRedisTemplate}, exposing the common
 * Redis operations as plain data calls. All keys and values are strings, matching how
 * the rest of the app uses Redis. HTTP concerns (status codes, response wrapping) stay
 * in the controller.
 */
public interface RedisService {

    // ----- Keys / generic -----
    Set<String> keys(String pattern);

    Long dbSize();

    Boolean exists(String key);

    String type(String key);

    Long ttl(String key);

    Boolean expire(String key, long seconds);

    Boolean persist(String key);

    /** Renames {@code key} to {@code newKey}; returns {@code false} if the source key is missing. */
    boolean rename(String key, String newKey);

    Boolean delete(String key);

    long deleteByPattern(String pattern);

    void flush();

    // ----- Strings -----
    String getString(String key);

    void setString(String key, String value, Long ttlSeconds);

    Long increment(String key, long by);

    // ----- Hashes -----
    Map<Object, Object> getHash(String key);

    Object getHashField(String key, String field);

    void setHashField(String key, String field, String value);

    void hSet(String key, Map<String, String> hash, Long ttlSeconds);

    Long deleteHashField(String key, String field);

    // ----- Lists -----
    List<String> getList(String key);

    /** Pushes onto the {@code left} or {@code right} end; returns the new length. */
    Long pushList(String key, String end, String value);

    /** Pops from the {@code left} or {@code right} end. */
    String popList(String key, String end);

    // ----- Sets -----
    Set<String> getSet(String key);

    Long addToSet(String key, String member);

    Long removeFromSet(String key, String member);

    // ----- Sorted sets -----
    Map<String, Double> getZSet(String key);

    Boolean addToZSet(String key, double score, String member);

    Long removeFromZSet(String key, String member);
}
