package com.devlabs.devlabsbackend.core.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer
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

        const val SHORT_TTL = 5L // 5 min
        const val MEDIUM_TTL = 10L // 10 min
        const val LONG_TTL = 30L // 30 min
        const val EXTRA_LONG_TTL = 120L // 2 hrs
    }

    /**
     * Creates a clean ObjectMapper for API responses without type information.
     */
    @Bean
    @Primary
    fun apiObjectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
            registerModule(JavaTimeModule())

            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)

            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
            configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true)

            val hibernate6Module = Hibernate6Module().apply {
                configure(Hibernate6Module.Feature.FORCE_LAZY_LOADING, false)
                configure(Hibernate6Module.Feature.USE_TRANSIENT_ANNOTATION, true)
                configure(Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS, true)
                configure(Hibernate6Module.Feature.REPLACE_PERSISTENT_COLLECTIONS, true)
            }
            registerModule(hibernate6Module)

            deactivateDefaultTyping()
        }
    }

    /**
     * Smart serializer that handles JSON conversion transparently.
     * Stores both type information and JSON data to enable proper deserialization.
     */
    @Bean
    fun typeAwareJsonRedisSerializer(): RedisSerializer<Any> {
        return TypeAwareJsonRedisSerializer(apiObjectMapper())
    }

    /**
     * Primary cache manager with smart JSON serialization.
     * Handles type-safe caching automatically without service code changes.
     */
    @Bean
    @Primary
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): CacheManager {
        val smartSerializer = typeAwareJsonRedisSerializer()
        
        val defaultConfig = RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(MEDIUM_TTL))
            .computePrefixWith { cacheName -> "devlabs:cache:$cacheName:" }
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(smartSerializer)
            )
            .disableCachingNullValues()

        // Configure specific cache TTLs
        val cacheConfigurations = mapOf(
            // Short TTL caches (5 minutes) - frequently changing data
            NOTIFICATION_CACHE to defaultConfig.entryTtl(Duration.ofMinutes(SHORT_TTL)),
            KANBAN_CACHE to defaultConfig.entryTtl(Duration.ofMinutes(SHORT_TTL)),

            // Medium TTL caches (10 minutes) - moderately changing data
            USER_CACHE to defaultConfig.entryTtl(Duration.ofMinutes(MEDIUM_TTL)),
            TEAM_CACHE to defaultConfig.entryTtl(Duration.ofMinutes(MEDIUM_TTL)),
            EVALUATION_CACHE to defaultConfig.entryTtl(Duration.ofMinutes(MEDIUM_TTL)),

            // Long TTL caches (30 minutes) - slowly changing data
            PROJECT_CACHE to defaultConfig.entryTtl(Duration.ofMinutes(LONG_TTL)),
            COURSE_CACHE to defaultConfig.entryTtl(Duration.ofMinutes(LONG_TTL)),
            RUBRICS_CACHE to defaultConfig.entryTtl(Duration.ofMinutes(LONG_TTL)),

            // Extra long TTL caches (2 hours) - rarely changing data
            DEPARTMENT_CACHE to defaultConfig.entryTtl(Duration.ofMinutes(EXTRA_LONG_TTL)),
            SEMESTER_CACHE to defaultConfig.entryTtl(Duration.ofMinutes(EXTRA_LONG_TTL))
        )

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware()
            .build()
    }
}

/**
 * Custom Redis serializer that automatically handles JSON serialization/deserialization
 * with type safety. Stores both the object type and JSON data to enable proper reconstruction.
 */
class TypeAwareJsonRedisSerializer(
    private val objectMapper: ObjectMapper
) : RedisSerializer<Any> {

    private val logger = org.slf4j.LoggerFactory.getLogger(TypeAwareJsonRedisSerializer::class.java)

    override fun serialize(value: Any?): ByteArray? {
        if (value == null) return null
        
        return try {
            // Create a wrapper with type information and JSON data
            val wrapper = mapOf(
                "type" to value::class.java.name,
                "data" to objectMapper.writeValueAsString(value)
            )
            
            logger.debug("🟢 CACHE STORE: ${value::class.java.simpleName}")
            objectMapper.writeValueAsBytes(wrapper)
        } catch (e: Exception) {
            // Log error and return null to indicate serialization failure
            logger.error("❌ CACHE STORE FAILED: ${e.message}")
            null
        }
    }

    override fun deserialize(bytes: ByteArray?): Any? {
        if (bytes == null || bytes.isEmpty()) return null
        
        return try {
            // Read the wrapper containing type and data
            @Suppress("UNCHECKED_CAST")
            val wrapper = objectMapper.readValue(bytes, Map::class.java) as Map<String, Any>
            val typeName = wrapper["type"] as? String ?: return null
            val jsonData = wrapper["data"] as? String ?: return null
            
            // Load the target class and deserialize
            val targetType = Class.forName(typeName)
            val result = objectMapper.readValue(jsonData, targetType)
            
            logger.info("🎯 CACHE HIT: ${targetType.simpleName}")
            result
        } catch (e: Exception) {
            // Log error and return null to indicate cache miss
            logger.warn("💔 CACHE MISS (deserialization failed): ${e.message}")
            null
        }
    }
}
