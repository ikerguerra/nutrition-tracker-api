package com.nutritiontracker.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        // Base config: String keys, JSON values
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .entryTtl(Duration.ofMinutes(60)) // Default TTL 1 hour
                .disableCachingNullValues();

        // Specific TTL configurations per cache name
        Map<String, RedisCacheConfiguration> specificConfigs = new HashMap<>();

        // Frequent Foods: changes less frequently, maybe calculated daily. Cache for 12
        // hours.
        specificConfigs.put("frequentFoods", defaultConfig.entryTtl(Duration.ofHours(12)));

        // Recent Foods: users might add logs relatively frequently. Cache for 1 hour.
        specificConfigs.put("recentFoods", defaultConfig.entryTtl(Duration.ofHours(1)));

        // Meal Templates: very static, cleared explicitly on update/delete
        specificConfigs.put("mealTemplates", defaultConfig.entryTtl(Duration.ofDays(7)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(specificConfigs)
                .build();
    }
}
