package com.nsdl.beckn.common.service;

import static com.nsdl.beckn.common.constant.ApplicationConstant.BECKN_API_LOOKUP_CACHE;
import static com.nsdl.beckn.common.constant.ApplicationConstant.PIPE;
import static com.nsdl.beckn.common.enums.OndcUserTypeNew.SELLER_APP;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.nsdl.beckn.api.model.lookup.LookupResponse;
import com.nsdl.beckn.api.model.lookup.NetworkParticipant;
import com.nsdl.beckn.api.model.lookup.SellerOnRecord;
import com.nsdl.beckn.common.cache.CacheManagerService;
import com.nsdl.beckn.common.model.LookupCacheModel;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LookupService {

	@Autowired
	private CacheManagerService cachingService;

	@Autowired
	private LookupCacheService lookupCacheService;

	@Autowired
	@Value("classpath:mock_lookup.json")
	private Resource resource;

	@Value("${beckn.entity.type}")
	private String entityType;

	@Value("${beckn.entity.id: }")
	private String entityId;

	@Value("${beckn.parameter.mock-lookup: false}")
	private boolean mockLookup;

	private static final String DATE_FORMAT_TZ = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	public LookupCacheModel getProvidersByKeyId(String cacheKey) {

		log.debug("finding matching provider for cacheKey[{}]", cacheKey);

		LookupCacheModel fromCache = (LookupCacheModel) this.cachingService.getFromCache(BECKN_API_LOOKUP_CACHE, cacheKey);
		if (fromCache != null) {
			log.info("data found in cache for cacheKey[{}]. no lookup call will be done", cacheKey);
			return fromCache;
		}

		log.warn("cacheKey {} not found in cache", cacheKey);

		// again make lookup and try getting the required record
		LookupResponse lookupResponse = this.lookupCacheService.lookupSingleSubscriber(cacheKey);
		LookupCacheModel freshLookupModel = buildLookupCacheModel(cacheKey, lookupResponse);

		log.info("fresh lookup response for cacheKey[{}] completed", cacheKey);

		return freshLookupModel;
	}

	private LookupCacheModel buildLookupCacheModel(String cacheKey, LookupResponse lookupResponse) {
		LookupCacheModel cacheModel = new LookupCacheModel();

		String[] array = cacheKey.split("\\|");
		String subscriberId = array[0];
		String uniqueKeyId = array[1];

		String match1 = lookupResponse.getSubscriberId() + PIPE + lookupResponse.getUniqueKeyId();
		String match2 = subscriberId + PIPE + uniqueKeyId;

		if (match1.equals(match2)) {
			cacheModel.setSubscriberId(lookupResponse.getSubscriberId());
			cacheModel.setUniqueKeyId(lookupResponse.getUniqueKeyId());
			cacheModel.setSigningPublicKey(lookupResponse.getSigningPublicKey());
			log.info("entity data will be used in sign verification {}", cacheModel);
		} else {
			List<NetworkParticipant> participants = lookupResponse.getNetworkParticipant();
			if (CollectionUtils.isNotEmpty(participants)) {
				for (NetworkParticipant participant : participants) {
					if (SELLER_APP.type().equalsIgnoreCase(participant.getType()) && isTrue(participant.getMsn())) {
						List<SellerOnRecord> sorList = participant.getSellerOnRecord();
						if (CollectionUtils.isNotEmpty(sorList)) {
							for (SellerOnRecord sor : sorList) {
								if ((lookupResponse.getSubscriberId() + PIPE + sor.getUniqueKeyId()).equals(match2)) {
									cacheModel.setSubscriberId(lookupResponse.getSubscriberId());
									cacheModel.setUniqueKeyId(sor.getUniqueKeyId());
									cacheModel.setSigningPublicKey(sor.getKeyPair().getSigningPublicKey());
									log.info("sor data will be used in sign verification {}", cacheModel);
								}
							}
						}
					}

				}
			}
		}

		return cacheModel;
	}

	private boolean isCacheValid(LookupResponse fromCache) {

		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_TZ);
		Date currentDate = new Date();

		Date validFrom = null;
		Date validUntil = null;
		try {
			validFrom = sdf.parse(fromCache.getValidFrom());
			validUntil = sdf.parse(fromCache.getValidUntil());
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}

		log.info("ValidFrom: {} & ValidUntil: {}", validFrom, validUntil);

		boolean isValidFrom = validFrom.before(currentDate);
		boolean isValidUntil = validUntil.after(currentDate);

		log.info("isValidFrom: {} & isValidUntil: {}", isValidFrom, isValidUntil);

		return isValidFrom && isValidUntil;
	}

	private String getMockLookupJson() {
		InputStream inputStream = null;
		try {
			inputStream = this.resource.getInputStream();
		} catch (IOException e) {
			log.error("error in getMockLookupJson", e);
		}
		String json = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
		log.info("mock lookup json is {}", json);
		return json;
	}

}
