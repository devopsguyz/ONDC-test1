package com.nsdl.beckn.common.service;

import static com.nsdl.beckn.common.constant.ApplicationConstant.ACTIVE_SELLERS;
import static com.nsdl.beckn.common.constant.ApplicationConstant.BECKN_API_LOOKUP_CACHE;
import static com.nsdl.beckn.common.constant.ApplicationConstant.BLANK;
import static com.nsdl.beckn.common.constant.ApplicationConstant.PIPE;
import static com.nsdl.beckn.common.enums.OndcUserType.GATEWAY;
import static com.nsdl.beckn.common.enums.OndcUserTypeNew.SELLER_APP;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.nsdl.beckn.api.model.lookup.LookupRequest;
import com.nsdl.beckn.api.model.lookup.LookupRequestParam;
import com.nsdl.beckn.api.model.lookup.LookupResponse;
import com.nsdl.beckn.api.model.lookup.NetworkParticipant;
import com.nsdl.beckn.api.model.lookup.SellerOnRecord;
import com.nsdl.beckn.common.cache.CacheManagerService;
import com.nsdl.beckn.common.model.ActiveSellerCacheModel;
import com.nsdl.beckn.common.model.LookupCacheModel;
import com.nsdl.beckn.common.model.SubscriberModel;
import com.nsdl.beckn.common.sender.Sender;
import com.nsdl.beckn.common.util.JsonUtil;
import com.nsdl.beckn.common.util.SigningUtility;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LookupCacheService {

	@Autowired
	private Sender sender;

	@Autowired
	private JsonUtil jsonUtil;

	@Autowired
	private HelperService helperService;

	@Autowired
	private CacheManagerService cachingService;

	@Autowired
	private SubscriberService subscriberService;

	@Autowired
	private SigningUtility signingUtility;

	@Value("${beckn.lookup.url:}")
	private String lookupUrl;

	@Value("${beckn.lookup.sor: true}")
	private boolean sorRequired;

	@Value("${beckn.entity.type}")
	private String entityType;

	@Value("${ehcache.cacheregion.beckn-api.lookup-cache.startup.domains:}")
	private List<String> domainList;

	private static final String DATE_FORMAT_TZ = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	private static final String IND = "IND";

	public void cacheLookupDataOnStartup() {
		log.warn("going to reload complete application cache");

		Collection<String> evictedCacheRegions = this.cachingService.evictAllCacheRegions();
		log.warn("cache evicted for regions {}", evictedCacheRegions);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_TZ);

		SubscriberModel subscriberModel = this.subscriberService.getActiveFirstSubscriber();

		this.domainList.forEach(domain -> {
			log.warn("loading lookup cache for domain {} with sorRequired {}", domain, this.sorRequired);

			LookupRequestParam requestParam = LookupRequestParam.builder().country(IND).domain(domain).sorRequired(this.sorRequired).build();

			String signString = requestParam.getCountry() + PIPE + requestParam.getDomain();
			String signature = this.signingUtility.generateSignature(signString, subscriberModel.getPrivateKey());

			LookupRequest lookupRequest = LookupRequest.builder()
					.requestId(UUID.randomUUID().toString())
					.senderSubscriberId(subscriberModel.getSubscriberId())
					.timestamp(formatter.format(LocalDateTime.now()))
					.searchParameters(requestParam)
					.signature(signature)
					.build();

			List<LookupResponse> list = lookup(domain, lookupRequest);
			log.info("lookup response list[size={}] for domain {} is {}", list.size(), domain, list);

			// now put in cache
			putInCache(list, domain);

			if (GATEWAY.type().equalsIgnoreCase(this.entityType)) {
				putInActiveSellerCache(list, domain);
			}

			log.warn("cache loaded for domain {}", domain);
		});

	}

	public LookupResponse lookupSingleSubscriber(String cacheKeyId) {

		String[] array = cacheKeyId.split("\\|");
		String subscriberId = array[0];
		String uniqueKeyId = array[1];
		String domain = array[2];

		log.warn("loading selective lookup cache for domain {} with sorRequired {}", domain, this.sorRequired);

		// LookupRequestParam requestParam = LookupRequestParam.builder().country(IND).domain(domain).subscriberId(subscriberId).uniqueKeyId(uniqueKeyId).build();
		LookupRequestParam requestParam = LookupRequestParam.builder().country(IND).domain(domain).subscriberId(subscriberId).sorRequired(this.sorRequired)
				.build();

		SubscriberModel subscriberModel = this.subscriberService.getActiveFirstSubscriber();

		String signString = requestParam.getCountry() + PIPE + requestParam.getDomain() + PIPE + requestParam.getSubscriberId();
		String signature = this.signingUtility.generateSignature(signString, subscriberModel.getPrivateKey());

		LookupRequest lookupRequest = LookupRequest.builder()
				.requestId(UUID.randomUUID().toString())
				.senderSubscriberId(subscriberModel.getSubscriberId())
				.timestamp(DateTimeFormatter.ofPattern(DATE_FORMAT_TZ).format(LocalDateTime.now()))
				.searchParameters(requestParam)
				.signature(signature)
				.build();

		List<LookupResponse> list = lookup(domain, lookupRequest);
		log.info("single subscriber lookup response list for key[{}] is {}", cacheKeyId, list);

		if (CollectionUtils.isEmpty(list)) {
			log.error("no data found in fresh lookup for key[{}] & request {}", cacheKeyId, lookupRequest);
			return new LookupResponse();
		}

		if (!uniqueKeyId.equals(list.get(0).getUniqueKeyId()) && !checkSellerOnRecordUkIds(subscriberId, uniqueKeyId, list.get(0))) {
			log.error("UniqueKeyId from header[{}] & UniqueKeyId from lookup at entity level[{}] or sor level does not match for subscriberId[{}]", uniqueKeyId,
					list.get(0).getUniqueKeyId(), subscriberId);
			return new LookupResponse();
		}

		// now put in cache
		putInCache(list, domain);

		if (GATEWAY.type().equalsIgnoreCase(this.entityType)) {
			putInActiveSellerCache(list, domain);
		}

		log.warn("cache updated for key {}", cacheKeyId);

		if (list.size() == 1) {
			return list.get(0);
		}
		log.error("expected 1 result but got {} when lookup was made for lookupRequest {}", list.size(), lookupRequest);
		return list.get(0);

	}

	private List<LookupResponse> lookup(String domain, LookupRequest request) {
		List<LookupResponse> lookupList = new ArrayList<>();

		log.debug("calling the lookup at url: {}", this.lookupUrl);
		String json = this.jsonUtil.toJson(request);

		log.info("lookup json to be send for domain[{}] is {}", domain, json);

		HttpHeaders headers = new HttpHeaders();

		String response = this.sender.send(this.lookupUrl, headers, json, null);
		log.info("lookup response json for domain[{}] & sorRequired[{}] is {}", domain, this.sorRequired, response);

		if (response != null) {
			lookupList = this.jsonUtil.toModelList(response, LookupResponse.class);
		}
		return lookupList;
	}

	private void putInCache(List<LookupResponse> lookupList, String domain) {
		lookupList.stream()
				.forEach(response -> {
					String subscriberId = response.getSubscriberId();

					List<NetworkParticipant> participantList = response.getNetworkParticipant();

					for (NetworkParticipant participant : participantList) {
						if (domain.equalsIgnoreCase(participant.getDomain())) {
							// set entity level in cache
							String ukId = isBlank(response.getUniqueKeyId()) ? BLANK : response.getUniqueKeyId();
							String entityKey = subscriberId + PIPE + ukId + PIPE + domain;

							this.cachingService.putToCache(BECKN_API_LOOKUP_CACHE, entityKey, buildCacheModelForEntity(response));

							if (!GATEWAY.type().equalsIgnoreCase(this.entityType)) {
								// setting keys for msn sellers
								List<SellerOnRecord> sellerOnRecords = this.helperService.findSellerOnRecord(participant, domain, subscriberId);
								for (SellerOnRecord record : sellerOnRecords) {

									String uniqueKeyId = record.getUniqueKeyId();

									if (isBlank(uniqueKeyId)) {
										log.error("no uniqueKeyId received for SubscriberId {}. so will not be put in cache", subscriberId);
										continue;
									}

									String sellerKey = subscriberId + PIPE + uniqueKeyId + PIPE + domain;
									this.cachingService.putToCache(BECKN_API_LOOKUP_CACHE, sellerKey, buildCacheModelForSeller(record, response));
								}
							} else {
								log.warn("seller_on_record for {} will not be put in cache for gateway", subscriberId);
							}
						} else {
							log.error("request to lookup was made for domain {} but also received {} in the reply for subscriber_id {}. "
									+ "so this will be skipped in cache", domain, participant.getDomain(), subscriberId);
						}

					}

				});

	}

	private LookupCacheModel buildCacheModelForSeller(SellerOnRecord sor, LookupResponse entity) {
		LookupCacheModel cacheModel = new LookupCacheModel();
		cacheModel.setSubscriberId(entity.getSubscriberId());
		cacheModel.setUniqueKeyId(sor.getUniqueKeyId());
		cacheModel.setSigningPublicKey(sor.getKeyPair().getSigningPublicKey());
		cacheModel.setCity(sor.getCityCode());
		cacheModel.setValidFrom(sor.getKeyPair().getValidFrom());
		cacheModel.setValidUntil(sor.getKeyPair().getValidUntil());
		return cacheModel;
	}

	private LookupCacheModel buildCacheModelForEntity(LookupResponse response) {
		LookupCacheModel cacheModel = new LookupCacheModel();
		cacheModel.setSubscriberId(response.getSubscriberId());
		cacheModel.setUniqueKeyId(response.getUniqueKeyId());
		cacheModel.setSigningPublicKey(response.getSigningPublicKey());
		cacheModel.setCity(response.getCity());
		cacheModel.setValidFrom(response.getValidFrom());
		cacheModel.setValidUntil(response.getValidUntil());
		return cacheModel;
	}

	private void putInActiveSellerCache(List<LookupResponse> list, String domain) {
		String cacheKey = ACTIVE_SELLERS + PIPE + domain;

		List<ActiveSellerCacheModel> sellerList = new ArrayList<>();

		for (LookupResponse response : list) {

			List<NetworkParticipant> participantList = response.getNetworkParticipant();

			if (isNotEmpty(participantList)) {
				for (NetworkParticipant participant : participantList) {
					if (SELLER_APP.type().equalsIgnoreCase(participant.getType())) {
						sellerList.add(buildActiveSellerCacheModel(participant, response.getSubscriberId()));
					}
				}
			}

		}
		// log.warn("count of active sellers that is going to be put in cache key[{}] is {}", cacheKey, sellerList.size());
		log.warn("active sellers with cache key[{}] with their cities {}", cacheKey, sellerList);
		this.cachingService.putToCache(BECKN_API_LOOKUP_CACHE, cacheKey, sellerList);
	}

	private ActiveSellerCacheModel buildActiveSellerCacheModel(NetworkParticipant participant, String subscriberId) {
		ActiveSellerCacheModel sellerCacheModel = new ActiveSellerCacheModel();

		sellerCacheModel.setSubscriberId(subscriberId);
		sellerCacheModel.setSubscriberUrl(participant.getSubscriberUrl());
		sellerCacheModel.setDomain(participant.getDomain());
		sellerCacheModel.setMsn(participant.getMsn());
		sellerCacheModel.setNetworkParticipantCityCodes(new HashSet<>(participant.getCityCode()));

		return sellerCacheModel;
	}

	private boolean checkSellerOnRecordUkIds(String subscriberId, String uniqueKeyId, LookupResponse lookupResponse) {
		List<NetworkParticipant> participants = lookupResponse.getNetworkParticipant();

		if (CollectionUtils.isEmpty(participants)) {
			return false;
		}

		for (NetworkParticipant participant : participants) {
			List<SellerOnRecord> onRecords = participant.getSellerOnRecord();

			if (CollectionUtils.isEmpty(onRecords)) {
				continue;
			}

			for (SellerOnRecord onRecord : onRecords) {
				if (uniqueKeyId.equals(onRecord.getUniqueKeyId())) {
					log.info("UniqueKeyId[{}] found in SellerOnRecord", uniqueKeyId);
					return true;
				}
			}
		}
		log.error("UniqueKeyId[{}] not found in SellerOnRecord of subscriberId[{}]", uniqueKeyId, subscriberId);
		return false;
	}

}
