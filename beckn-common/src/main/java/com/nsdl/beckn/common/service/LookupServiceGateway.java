package com.nsdl.beckn.common.service;

import static com.nsdl.beckn.common.constant.ApplicationConstant.ACTIVE_SELLERS;
import static com.nsdl.beckn.common.constant.ApplicationConstant.BECKN_API_LOOKUP_CACHE;
import static com.nsdl.beckn.common.constant.ApplicationConstant.PIPE;
import static com.nsdl.beckn.common.constant.ApplicationConstant.STAR;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.nsdl.beckn.common.cache.CacheManagerService;
import com.nsdl.beckn.common.model.ActiveSellerCacheModel;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LookupServiceGateway {

	@Autowired
	private CacheManagerService cachingService;

	@Autowired
	private HelperService helperService;

	@Autowired
	private LookupCacheService lookupCacheService;

	@Autowired
	@Value("classpath:mock_lookup.json")
	private Resource resource;

	@Value("${beckn.parameter.mock-lookup: false}")
	private boolean mockLookup;

	@Value("${beckn.parameter.city-filter: false}")
	private boolean cityFilter;

	public List<ActiveSellerCacheModel> getActiveSellers(String subscriberId, String domain, String city) {
		log.info("going to find active sellers for domain {}", domain);

		String cacheKey = ACTIVE_SELLERS + PIPE + domain;

		log.info("cache key to find active sellers for given domain is {}", cacheKey);

		List<ActiveSellerCacheModel> lookupList = (List<ActiveSellerCacheModel>) this.cachingService.getFromCache(BECKN_API_LOOKUP_CACHE, cacheKey);

		if (CollectionUtils.isEmpty(lookupList)) {
			log.error("no active sellers found in cache");

			// again make lookup and try getting the required record
			this.lookupCacheService.cacheLookupDataOnStartup();
			lookupList = (List<ActiveSellerCacheModel>) this.cachingService.getFromCache(BECKN_API_LOOKUP_CACHE, cacheKey);
		}

		if (CollectionUtils.isEmpty(lookupList)) {
			log.error("despite making another lookup call, no active sellers found for key {}", cacheKey);
			return new ArrayList<>();
		}

		log.info("total lookup list to find active sellers found in cache with size {}", lookupList.size());
		List<ActiveSellerCacheModel> sellerUrlList = lookupList.stream()
				.filter(model -> this.helperService.isParticipantInDomainAndSeller(model, domain))
				// .peek(data -> log.info("filtered 1-> {}", data))
				.filter(model -> cityCheck(city, model))
				// .peek(data -> log.info("filtered 2-> {}", data))
				.collect(Collectors.toList());

		log.info("count of active seller after applying city filter[{}] for doman[{}] is {}", this.cityFilter, domain, sellerUrlList.size());

		return sellerUrlList;
	}

	private boolean cityCheck(String requestCity, ActiveSellerCacheModel model) {

		if (this.cityFilter) {
			if (requestCity == null) {
				log.error("since requested city is null, returning false");
				return false;
			}

			boolean match = model.getNetworkParticipantCityCodes()
					.stream()
					.anyMatch(lookupCity -> lookupCity.trim().equalsIgnoreCase(requestCity.trim()) || STAR.equals(lookupCity.trim()));

			log.info("is city matched ? {}", match);
			return match;
		}

		log.info("since city filter is off, returning true");
		return true;

	}

}
