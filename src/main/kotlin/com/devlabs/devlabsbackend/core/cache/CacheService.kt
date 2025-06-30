package com.devlabs.devlabsbackend.core.cache

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class CacheService {

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, Any>

    @Autowired
    private lateinit var cacheManager: CacheManager

    /**
     * Store a value in cache with custom TTL
     */
    fun put(key: String, value: Any, ttl: Long, timeUnit: TimeUnit = TimeUnit.MINUTES) {
        redisTemplate.opsForValue().set(key, value, ttl, timeUnit)
    }

    /**
     * Get a value from cache
     */
    fun get(key: String): Any? {
        return redisTemplate.opsForValue().get(key)
    }

    /**
     * Get a value from cache with type casting
     */
    inline fun <reified T> get(key: String, clazz: Class<T>): T? {
        val value = redisTemplate.opsForValue().get(key)
        return if (value != null && clazz.isInstance(value)) {
            clazz.cast(value)
        } else null
    }

    /**
     * Delete a key from cache
     */
    fun delete(key: String): Boolean {
        return redisTemplate.delete(key)
    }

    /**
     * Delete multiple keys from cache
     */
    fun delete(keys: Collection<String>): Long {
        return redisTemplate.delete(keys)
    }

    /**
     * Check if key exists in cache
     */
    fun exists(key: String): Boolean {
        return redisTemplate.hasKey(key)
    }

    /**
     * Set expiration time for a key
     */
    fun expire(key: String, ttl: Long, timeUnit: TimeUnit = TimeUnit.MINUTES): Boolean {
        return redisTemplate.expire(key, ttl, timeUnit)
    }

    /**
     * Get keys matching a pattern
     */
    fun getKeys(pattern: String): Set<String> {
        return redisTemplate.keys(pattern)
    }

    /**
     * Clear specific cache by name
     */
    fun clearCache(cacheName: String) {
        cacheManager.getCache(cacheName)?.clear()
    }

    /**
     * Clear all caches
     */
    fun clearAllCaches() {
        cacheManager.cacheNames.forEach { cacheName ->
            cacheManager.getCache(cacheName)?.clear()
        }
    }

    /**
     * Store in hash
     */
    fun putHash(key: String, hashKey: String, value: Any) {
        redisTemplate.opsForHash<String, Any>().put(key, hashKey, value)
    }

    /**
     * Get from hash
     */
    fun getHash(key: String, hashKey: String): Any? {
        return redisTemplate.opsForHash<String, Any>().get(key, hashKey)
    }

    /**
     * Get all hash entries
     */
    fun getAllHash(key: String): Map<String, Any> {
        return redisTemplate.opsForHash<String, Any>().entries(key)
    }

    /**
     * Delete from hash
     */
    fun deleteHash(key: String, hashKey: String): Long {
        return redisTemplate.opsForHash<String, Any>().delete(key, hashKey)
    }

    /**
     * Add to set
     */
    fun addToSet(key: String, value: Any): Long {
        return redisTemplate.opsForSet().add(key, value) ?: 0
    }

    /**
     * Get set members
     */
    fun getSetMembers(key: String): Set<Any> {
        return redisTemplate.opsForSet().members(key) ?: emptySet()
    }

    /**
     * Remove from set
     */
    fun removeFromSet(key: String, value: Any): Long {
        return redisTemplate.opsForSet().remove(key, value) ?: 0
    }

    /**
     * Check if member exists in set
     */
    fun isSetMember(key: String, value: Any): Boolean {
        return redisTemplate.opsForSet().isMember(key, value) ?: false
    }
}
