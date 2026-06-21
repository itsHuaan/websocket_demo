package com.example.websocket_demo.service.redis.impl;

import com.example.websocket_demo.common.DataUtil;
import com.example.websocket_demo.service.redis.IRedisService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisServiceImpl implements IRedisService {
    StringRedisTemplate redisTemplate;

    // ----- Keys / generic -----

    @Override
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    @Override
    public Long dbSize() {
        return redisTemplate.execute((RedisCallback<Long>) connection -> connection.serverCommands().dbSize());
    }

    @Override
    public Boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public String type(String key) {
        DataType type = redisTemplate.type(key);
        return type != null ? type.code() : "none";
    }

    @Override
    public Long ttl(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    @Override
    public Boolean expire(String key, long seconds) {
        return redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }

    @Override
    public Boolean persist(String key) {
        return redisTemplate.persist(key);
    }

    @Override
    public boolean rename(String key, String newKey) {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            return false;
        }
        redisTemplate.rename(key, newKey);
        return true;
    }

    @Override
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    @Override
    public long deleteByPattern(String pattern) {
        Set<String> matches = redisTemplate.keys(pattern);
        return (matches == null || matches.isEmpty()) ? 0 : redisTemplate.delete(matches);
    }

    @Override
    public void flush() {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.serverCommands().flushDb();
            return null;
        });
    }

    // ----- Strings -----

    @Override
    public String getString(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void setString(String key, String value, Long ttlSeconds) {
        String val = value != null ? value : "";
        if (ttlSeconds != null && ttlSeconds > 0) {
            redisTemplate.opsForValue().set(key, val, ttlSeconds, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(key, val);
        }
    }

    @Override
    public Long increment(String key, long by) {
        return redisTemplate.opsForValue().increment(key, by);
    }

    // ----- Hashes -----

    @Override
    public Map<Object, Object> getHash(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    @Override
    public Object getHashField(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    @Override
    public void setHashField(String key, String field, String value) {
        redisTemplate.opsForHash().put(key, field, value != null ? value : "");
    }

    @Override
    public void hSet(String key, Map<String, String> hash, Long ttlSeconds) {
        redisTemplate.opsForHash().putAll(key, hash);
        if (!isPersistent(ttlSeconds)) {
            expire(key, ttlSeconds);
        }
    }

    @Override
    public Long deleteHashField(String key, String field) {
        return redisTemplate.opsForHash().delete(key, field);
    }

    // ----- Lists -----

    @Override
    public List<String> getList(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    @Override
    public Long pushList(String key, String end, String value) {
        String val = value != null ? value : "";
        return "left".equalsIgnoreCase(end)
                ? redisTemplate.opsForList().leftPush(key, val)
                : redisTemplate.opsForList().rightPush(key, val);
    }

    @Override
    public String popList(String key, String end) {
        return "right".equalsIgnoreCase(end)
                ? redisTemplate.opsForList().rightPop(key)
                : redisTemplate.opsForList().leftPop(key);
    }

    // ----- Sets -----

    @Override
    public Set<String> getSet(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    @Override
    public Long addToSet(String key, String member) {
        return redisTemplate.opsForSet().add(key, member != null ? member : "");
    }

    @Override
    public Long removeFromSet(String key, String member) {
        return redisTemplate.opsForSet().remove(key, member);
    }

    // ----- Sorted sets -----

    @Override
    public Map<String, Double> getZSet(String key) {
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);
        Map<String, Double> result = new LinkedHashMap<>();
        if (tuples != null) {
            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                result.put(tuple.getValue(), tuple.getScore());
            }
        }
        return result;
    }

    @Override
    public Boolean addToZSet(String key, double score, String member) {
        return redisTemplate.opsForZSet().add(key, member != null ? member : "", score);
    }

    @Override
    public Long removeFromZSet(String key, String member) {
        return redisTemplate.opsForZSet().remove(key, member);
    }

    private boolean isPersistent(Long ttlSeconds){
        return DataUtil.isNullOrZero(ttlSeconds);
    }
}
