package com.servicedesk.marketplace.plugin;

import java.time.Duration;
import java.util.Optional;

/**
 * Cache service interface for modules
 */
public interface ModuleCacheService {

    /**
     * Get value from cache
     */
    <T> Optional<T> get(String key, Class<T> type);

    /**
     * Put value in cache with default TTL
     */
    void put(String key, Object value);

    /**
     * Put value in cache with custom TTL
     */
    void put(String key, Object value, Duration ttl);

    /**
     * Remove value from cache
     */
    void evict(String key);

    /**
     * Remove all values with given prefix
     */
    void evictByPrefix(String prefix);

    /**
     * Check if key exists in cache
     */
    boolean exists(String key);

    /**
     * Get or compute value if not present
     */
    <T> T getOrCompute(String key, Class<T> type, java.util.function.Supplier<T> supplier);

    /**
     * Get or compute value if not present with TTL
     */
    <T> T getOrCompute(String key, Class<T> type, java.util.function.Supplier<T> supplier, Duration ttl);
}
