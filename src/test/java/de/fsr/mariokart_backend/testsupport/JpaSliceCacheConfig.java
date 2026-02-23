package de.fsr.mariokart_backend.testsupport;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class JpaSliceCacheConfig {

    @Bean
    CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("registration", "schedule", "settings", "survey");
    }
}
