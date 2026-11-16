package com.cinema.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存工具。
 * 本机 Redis 可能未启动, 所有读写均做降级处理: 缓存不可用时直接回源数据库, 不影响业务。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheService {

    private static final String PREFIX = "cinema:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public <T> T get(String key, Class<T> clazz) {
        try {
            String json = redisTemplate.opsForValue().get(PREFIX + key);
            if (json == null || json.isEmpty()) {
                return null;
            }
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.debug("Redis 读取失败, 已降级: key={}, msg={}", key, e.getMessage());
            return null;
        }
    }

    public <T> List<T> getList(String key, Class<T> clazz) {
        try {
            String json = redisTemplate.opsForValue().get(PREFIX + key);
            if (json == null || json.isEmpty()) {
                return null;
            }
            return objectMapper.readValue(json, new TypeReference<List<T>>() {
            });
        } catch (Exception e) {
            log.debug("Redis 读取失败, 已降级: key={}, msg={}", key, e.getMessage());
            return null;
        }
    }

    public void set(String key, Object value, long minutes) {
        try {
            redisTemplate.opsForValue().set(PREFIX + key, objectMapper.writeValueAsString(value),
                    minutes, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.debug("Redis 写入失败, 已忽略: key={}, msg={}", key, e.getMessage());
        }
    }

    public void deleteByPrefix(String prefix) {
        try {
            Set<String> keys = redisTemplate.keys(PREFIX + prefix + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.debug("Redis 清理失败, 已忽略: prefix={}, msg={}", prefix, e.getMessage());
        }
    }

    public List<String> safeList(List<String> list) {
        return list == null ? Collections.emptyList() : list;
    }
}
