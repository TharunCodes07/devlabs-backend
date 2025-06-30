package com.devlabs.devlabsbackend.core.config

import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
class CacheConfig {

    companion object {
        const val USER_CACHE = "users"
        const val DEPARTMENT_CACHE = "departments"
        const val COURSE_CACHE = "courses"
        const val PROJECT_CACHE = "projects"
        const val TEAM_CACHE = "teams"
        const val EVALUATION_CACHE = "evaluations"
        const val RUBRICS_CACHE = "rubrics"
        const val SEMESTER_CACHE = "semesters"
        const val NOTIFICATION_CACHE = "notifications"
        const val KANBAN_CACHE = "kanban"
        
        // Cache TTL configurations (in minutes)
        const val SHORT_TTL = 5L // 5 min
        const val MEDIUM_TTL = 30L // 30 min
        const val LONG_TTL = 50L // 60 min
        const val EXTRA_LONG_TTL = 120L // 2 hrs
    }

    @Bean
    fun customCacheManager(redisConnectionFactory: RedisConnectionFactory): CacheManager {
        val defaultConfig = RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(MEDIUM_TTL))
            .computePrefixWith { cacheName -> "devlabs:$cacheName:" }
            .serializeKeysWith(
                org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                    .fromSerializer(StringRedisSerializer())
            )
            .serializeValuesWith(
                org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                    .fromSerializer(GenericJackson2JsonRedisSerializer())
            )

        val cacheConfigurations = mapOf(
            // Short-lived caches (5 minutes)
            NOTIFICATION_CACHE to defaultConfig.entryTtl(Duration.ofMinutes(SHORT_TTL)),
            KANBAN_CACHE to defaultConfig.entryTtl(Duration.ofMinutes(SHORT_TTL)),
            
            // Medium-lived caches (30 minutes)
            USER_CACHE to defaultConfig.entryTtl(Duration.ofMinutes(MEDIUM_TTL)),
            TEAM_CACHE to defaultConfig.entryTtl(Duration.ofMinutes(MEDIUM_TTL)),
            EVALUATION_CACHE to defaultConfig.entryTtl(Duration.ofMinutes(MEDIUM_TTL)),
            
            // Long-lived caches (1 hour)
            PROJECT_CACHE to defaultConfig.entryTtl(Duration.ofMinutes(LONG_TTL)),
            COURSE_CACHE to defaultConfig.entryTtl(Duration.ofMinutes(LONG_TTL)),
            RUBRICS_CACHE to defaultConfig.entryTtl(Duration.ofMinutes(LONG_TTL)),
            
            // Extra long-lived caches (4 hours)
            DEPARTMENT_CACHE to defaultConfig.entryTtl(Duration.ofMinutes(EXTRA_LONG_TTL)),
            SEMESTER_CACHE to defaultConfig.entryTtl(Duration.ofMinutes(EXTRA_LONG_TTL))
        )

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }
}
