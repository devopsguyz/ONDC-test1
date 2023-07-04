package com.nsdl.beckn.common.cache;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CacheManagerService {

	@Autowired
	private CacheManager cacheManager;

	public void putToCache(String cacheName, String key, Object value) {
		log.debug("putting in cache {} with key {}", cacheName, key);
		this.cacheManager.getCache(cacheName).put(key, value);
	}

	public Object getFromCache(String cacheName, String key) {
		ValueWrapper wrapper = this.cacheManager.getCache(cacheName).get(key);
		if (wrapper != null) {
			Object object = wrapper.get();
			log.debug("The value of getFromCache is {}", object);
			return object;
		}
		return null;
	}

	public String getFromCache1(String cacheName, String key) {
		String value = null;
		if (this.cacheManager.getCache(cacheName).get(key) != null) {
			value = this.cacheManager.getCache(cacheName).get(key).get().toString();
		}
		return value;
	}

	@CacheEvict(value = "first", key = "#cacheKey")
	public void evictSingleCacheValue(String cacheKey) {
	}

	@CacheEvict(value = "first", allEntries = true)
	public void evictAllCacheValues() {
	}

	public boolean evictSingleCacheValue(String cacheName, String cacheKey) {
		return this.cacheManager.getCache(cacheName).evictIfPresent(cacheKey);
	}

	public boolean evictAllCacheValues(String cacheName) {
		log.info("received request to evict cache region {}", cacheName);
		Cache cache = this.cacheManager.getCache(cacheName);
		if (cache != null) {
			cache.clear();
			log.info("cache region {} cleaned", cacheName);
			return true;
		}
		log.warn("cache region {} not cleaned", cacheName);
		return false;
	}

	public Collection<String> evictAllCacheRegions() {
		log.info("received request to evict all cache region");
		Collection<String> cacheNames = this.cacheManager.getCacheNames();

		for (String cacheName : cacheNames) {
			Cache cache = this.cacheManager.getCache(cacheName);
			if (cache != null) {
				cache.clear();
				log.info("cache region {} cleaned", cacheName);
			}

		}
		log.warn("cache cleared for regions {}", cacheNames);
		return cacheNames;
	}

	public void evictAllCaches() {
		this.cacheManager.getCacheNames()
				.parallelStream()
				.forEach(cacheName -> this.cacheManager.getCache(cacheName).clear());
	}

	public Collection<Cache> getCacheRegions(String name) {

		Collection<Cache> list = new ArrayList<>();

		log.info("received request to get cache for region {}", name);
		Collection<String> cacheNames = this.cacheManager.getCacheNames();

		for (String cacheName : cacheNames) {
			Cache cache = this.cacheManager.getCache(cacheName);
			log.info("cache data for region {} is {}", cacheName, cache);

			Object nativeCache = cache.getNativeCache();
			log.info("nativeCache data is {}", nativeCache);

			list.add(cache);
		}
		log.warn("cache list to return {}", list);
		return list;
	}

	// @Scheduled(fixedRate = 600000)
	public void evictAllcachesAtIntervals() {
		evictAllCaches();
	}
}