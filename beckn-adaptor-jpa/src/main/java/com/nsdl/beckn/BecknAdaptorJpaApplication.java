package com.nsdl.beckn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Scheduled;

import com.nsdl.beckn.common.service.LookupCacheService;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class BecknAdaptorJpaApplication implements CommandLineRunner {

	@Autowired
	private LookupCacheService lookupCacheService;

	@Value("${ehcache.cacheregion.beckn-api.lookup-cache.startup.load: false}")
	private boolean loadOnStart;

	@Value("${ehcache.cacheregion.beckn-api.lookup-cache.scheduled.enabled: true}")
	private boolean cacheCronEnabled;

	public static void main(String[] args) {
		SpringApplication.run(BecknAdaptorJpaApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		if (this.loadOnStart) {
			log.warn("load lookup-cache on start is switched on");
			try {
				this.lookupCacheService.cacheLookupDataOnStartup();
			} catch (Exception e) {
				log.error("lookup-cache loading at start failed", e);
				e.printStackTrace();
			}
		} else {
			log.warn("load lookup-cache on start is switched off");
		}
	}

	@Scheduled(cron = "${ehcache.cacheregion.beckn-api.lookup-cache.scheduled.cron}")
	public void reloadCache() {
		if (this.cacheCronEnabled) {
			log.warn("lookup cache scheduled reload cron is enabled");
			this.lookupCacheService.cacheLookupDataOnStartup();
		} else {
			log.warn("lookup cache scheduled reload cron is disabled");
		}
	}

}
