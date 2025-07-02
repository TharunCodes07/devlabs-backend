package com.devlabs.devlabsbackend.core.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.CollectionType
import com.fasterxml.jackson.databind.type.MapType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
@EnableCaching
class RedisConfig {

    @Value("\${spring.data.redis.host}")
    private lateinit var redisHost: String

    @Value("\${spring.data.redis.port}")
    private var redisPort: Int = 6379

    @Value("\${spring.data.redis.password}")
    private lateinit var redisPassword: String

    @Value("\${spring.data.redis.database}")
    private var redisDatabase: Int = 0

    @Value("\${spring.data.redis.timeout}")
    private lateinit var redisTimeout: String

    @Value("\${spring.cache.redis.time-to-live}")
    private var cacheTimeToLive: Long = 600000

    @Value("\${spring.cache.redis.key-prefix}")
    private lateinit var cacheKeyPrefix: String

    @Bean
    @Primary
    fun redisConnectionFactory(): RedisConnectionFactory {
        val config = RedisStandaloneConfiguration()
        config.hostName = redisHost
        config.port = redisPort
        config.password = RedisPassword.of(redisPassword)
        config.database = redisDatabase

        val poolConfig = GenericObjectPoolConfig<Any>()
        poolConfig.maxTotal = 10
        poolConfig.maxIdle = 10
        poolConfig.minIdle = 1
        poolConfig.testOnBorrow = true
        poolConfig.testOnReturn = true
        poolConfig.testWhileIdle = true

        val clientConfig = LettucePoolingClientConfiguration.builder()
            .poolConfig(poolConfig)
            .commandTimeout(Duration.ofMillis(parseTimeout(redisTimeout)))
            .build()

        return LettuceConnectionFactory(config, clientConfig)
    }

    private fun parseTimeout(timeout: String): Long {
        return when {
            timeout.endsWith("ms") -> timeout.removeSuffix("ms").toLong()
            timeout.endsWith("s") -> timeout.removeSuffix("s").toLong() * 1000
            timeout.endsWith("m") -> timeout.removeSuffix("m").toLong() * 60 * 1000
            else -> timeout.toLong()
        }
    }
}
