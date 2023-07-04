package com.nsdl.beckn.common.config;

import static com.nsdl.beckn.common.constant.ApplicationConstant.BECKN_API_COMMON_CACHE;
import static com.nsdl.beckn.common.constant.ApplicationConstant.PIPE;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.EnumSet;

import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.event.EventType;
import org.ehcache.impl.config.event.DefaultCacheEventListenerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import com.nsdl.beckn.common.cache.CacheEventLogger;

import lombok.extern.slf4j.Slf4j;

// @EnableCaching
// @Configuration
@Slf4j
public class CacheConfig extends CachingConfigurerSupport {

	private static final String CACHE_PATH = "ehcache.cacheregion.beckn-api.";

	@Autowired
	private Environment env;

	@Bean
	public CacheManager ehCacheManager() {
		log.info("[Ehcache configuration initialization <start>]");

		int entryCount = this.env.getProperty(CACHE_PATH + "common-cache.entrycount", Integer.class);

		// Configure default cache properties
		CacheConfiguration<String, String> cacheConfiguration = CacheConfigurationBuilder.newCacheConfigurationBuilder(
				// Numeric types of cached data K and V
				// The cache key value type must be specified in ehcache3.3, if the type in use is
				// different from the configuration, a class conversion
				// exception will be reported
				String.class, String.class,
				ResourcePoolsBuilder
						.newResourcePoolsBuilder()
						// Set the cache heap to accommodate the number of elements (JVM memory
						// space) after exceeding the number will be stored in offheap
						.heap(entryCount, EntryUnit.ENTRIES)
						// Configure disk persistent storage (hard disk storage) to persist to disk,
						// set to false here to not enable
						// .disk(500L, MemoryUnit.MB, false)
						// Set the off-heap storage size (memory storage). If the size exceeds
						// offheap, the rule will be eliminated.
						.offheap(100L, MemoryUnit.MB))
				.withService(getEventListenerConfiguation())
				.withExpiry(ExpiryPolicyBuilder
						.timeToLiveExpiration(Duration.ofMinutes(this.env.getProperty(CACHE_PATH + "common-cache.timetolive", Integer.class))))
				.build();

		// CacheManager manages the cache
		CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
				// Hard disk persistent address
				// .with(CacheManagerBuilder.persistence("D:/ehcacheData"))
				// Set a default cache configuration
				.withCache(BECKN_API_COMMON_CACHE, cacheConfiguration)
				// Initialize immediately after creation
				.build(true);

		log.info("[Ehcache configuration initialization <complete>]");
		return cacheManager;
	}

	private DefaultCacheEventListenerConfiguration getEventListenerConfiguation() {
		return CacheEventListenerConfigurationBuilder
				.newEventListenerConfiguration(cacheEvent -> log.warn("Policy " + cacheEvent.getOldValue()
						+ " has been evicted. Check your max heap size settings"), EventType.CREATED, EventType.UPDATED, EventType.EXPIRED, EventType.EVICTED,
						EventType.REMOVED)
				.build();
	}
	private CacheEventListenerConfigurationBuilder getEventListenerConfiguation1() {
		return CacheEventListenerConfigurationBuilder
				.newEventListenerConfiguration(new CacheEventLogger(),
						EnumSet.of(EventType.CREATED, EventType.UPDATED, EventType.EXPIRED, EventType.EVICTED, EventType.REMOVED))
				.unordered()
				.asynchronous();
	}

	@Override
	public KeyGenerator keyGenerator() {
		return new SimpleKeyGenerator() {
			@Override
			public Object generate(Object target, Method method, Object... params) {
				return target.getClass().getName() + PIPE + method.getName() + PIPE + generateKey(params);
			}
		};
	}

}