package com.nsdl.beckn.common.cache;
import static com.nsdl.beckn.common.constant.ApplicationConstant.BECKN_API_BLACKLIST_CACHE;
import static com.nsdl.beckn.common.constant.ApplicationConstant.BECKN_API_COMMON_CACHE;
import static com.nsdl.beckn.common.constant.ApplicationConstant.BECKN_API_LOOKUP_CACHE;

import java.time.Duration;
import java.util.EnumSet;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.event.EventType;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import lombok.extern.slf4j.Slf4j;

@EnableCaching
@Configuration
@Slf4j
public class ApplicationCacheConfig extends CachingConfigurerSupport {

	private static final String CACHE_PATH_COMMON = "ehcache.cacheregion.beckn-api.common-cache.";
	private static final String CACHE_PATH_LOOKUP = "ehcache.cacheregion.beckn-api.lookup-cache.";
	private static final String CACHE_PATH_BLACKLIST = "ehcache.cacheregion.beckn-api.blacklist-cache.";

	@Autowired
	private Environment env;

	@Bean
	public CacheManager ehCacheManager() {

		CachingProvider provider = Caching.getCachingProvider();
		CacheManager cacheManager = provider.getCacheManager();

		createCommonCache(cacheManager);
		createLookupCache(cacheManager);
		createBlackListCache(cacheManager);

		return cacheManager;
	}

	private void createCommonCache(CacheManager cacheManager) {
		int entryCount = this.env.getProperty(CACHE_PATH_COMMON + "entrycount", Integer.class);
		int timetolive = this.env.getProperty(CACHE_PATH_COMMON + "timetolive", Integer.class);
		log.info("common-cache.entrycount: {} & common-cache.timetolive {}", entryCount, timetolive);
		CacheConfigurationBuilder<Object, Object> configurationBuilder = buildConfiguration(entryCount, timetolive);

		// create caches we need
		cacheManager.createCache(BECKN_API_COMMON_CACHE,
				Eh107Configuration.fromEhcacheCacheConfiguration(configurationBuilder.withService(getAsynchronousListener())));
	}

	private void createLookupCache(CacheManager cacheManager) {
		int entryCount = this.env.getProperty(CACHE_PATH_LOOKUP + "entrycount", Integer.class);
		int timetolive = this.env.getProperty(CACHE_PATH_LOOKUP + "timetolive", Integer.class);
		log.info("lookup-cache.entrycount: {} & lookup-cache.timetolive {}", entryCount, timetolive);
		CacheConfigurationBuilder<Object, Object> configurationBuilder = buildConfiguration(entryCount, timetolive);

		// create caches we need
		cacheManager.createCache(BECKN_API_LOOKUP_CACHE,
				Eh107Configuration.fromEhcacheCacheConfiguration(configurationBuilder.withService(getAsynchronousListener())));
	}

	private void createBlackListCache(CacheManager cacheManager) {
		int entryCount = this.env.getProperty(CACHE_PATH_BLACKLIST + "entrycount", Integer.class);
		int timetolive = this.env.getProperty(CACHE_PATH_BLACKLIST + "timetolive", Integer.class);
		log.info("blacklist-cache.entrycount: {} & blacklist-cache.timetolive {}", entryCount, timetolive);
		CacheConfigurationBuilder<Object, Object> configurationBuilder = buildConfiguration(entryCount, timetolive);

		// create caches we need
		cacheManager.createCache(BECKN_API_BLACKLIST_CACHE,
				Eh107Configuration.fromEhcacheCacheConfiguration(configurationBuilder.withService(getAsynchronousListener())));
	}

	private CacheConfigurationBuilder<Object, Object> buildConfiguration(int entryCount, int timetolive) {

		return CacheConfigurationBuilder
				.newCacheConfigurationBuilder(Object.class, Object.class, ResourcePoolsBuilder.heap(entryCount))
				.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(timetolive)));
	}

	@Override
	@Bean("customKeyGenerator")
	public KeyGenerator keyGenerator() {
		return new CustomKeyGenerator();
	}

	private CacheEventListenerConfigurationBuilder getAsynchronousListener() {
		return CacheEventListenerConfigurationBuilder
				.newEventListenerConfiguration(new CacheEventLogger(),
						EnumSet.of(EventType.CREATED, EventType.UPDATED, EventType.EXPIRED, EventType.EVICTED, EventType.REMOVED))
				.unordered()
				.asynchronous();
	}
}