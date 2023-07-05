package com.nsdl.beckn.adaptor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.nsdl.beckn.common.cache.CacheManagerService;
import com.nsdl.beckn.common.model.CacheModel;
import com.nsdl.beckn.common.service.AuditDbService;
import com.nsdl.beckn.common.service.LookupCacheService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class UtilController {

	private static final String ALL = "all";

	@Autowired
	private CacheManagerService cachingService;

	@Autowired
	private LookupCacheService lookupCacheService;

	@Autowired
	private AuditDbService auditDbService;

	@PostMapping("/cache-evict")
	public Object evitCache(@RequestBody CacheModel model) {

		log.info("CacheModel is {}", model);

		if (ALL.equalsIgnoreCase(model.getKey())) {
			return this.cachingService.evictAllCacheRegions();
		}

		return this.cachingService.evictAllCacheValues(model.getKey());
	}

	@PostMapping("/cache-reload")
	public boolean reloadCache() {
		log.info("going to do cache reload");
		try {
			this.lookupCacheService.cacheLookupDataOnStartup();
		} catch (Exception e) {
			log.error("cache reload failed", e);
			return false;
		}

		return true;
	}

	@PostMapping("/cache-get")
	public Object getCache(@RequestBody CacheModel model) {
		log.info("going to get cache for data {}", model);

		String name = model.getName();
		String key = model.getKey();

		try {
			Object fromCache = null;

			if ("lookup".equalsIgnoreCase(name)) {
				fromCache = this.cachingService.getFromCache("beckn-api-cache-lookup", key);
			} else if ("common".equalsIgnoreCase(name)) {
				fromCache = this.cachingService.getFromCache("beckn-api-cache-common", key);
			} else if ("blacklist".equalsIgnoreCase(name)) {
				fromCache = this.cachingService.getFromCache("beckn-api-cache-blacklist", key);
			}

			if (fromCache != null) {
				return fromCache;
			}

			return "no entry found in " + name + " for key " + key;

		} catch (Exception e) {
			log.error("get cache failed for key {}", key);
			log.error("error in getCache is", e);
		}
		return "exception occured";
	}

	@GetMapping("/health")
	public ResponseEntity<String> health() {
		String health = this.auditDbService.health();
		log.info("health status is {}", health);
		return ResponseEntity.ok(health);
	}

}
