package com.github.switcherapi.ac.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@EnableCaching
@Configuration
public class CacheConfig implements CachingConfigurer {

	private final long duration;
	private final boolean enabled;

	public CacheConfig(
			@Value("${service.cache.duration:1}") long duration,
			@Value("${service.cache.enabled:true}") boolean enabled) {
		this.duration = duration;
		this.enabled = enabled;
	}

	@Bean
	@Override
	public CacheManager cacheManager() {
		if (!enabled) {
			return new NoOpCacheManager();
		}

		var cacheManager = new CaffeineCacheManager();
		cacheManager.setAsyncCacheMode(true);
		cacheManager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(duration, TimeUnit.MINUTES));
		return cacheManager;
	}
}