package com.nsdl.beckn.common.service;

import static com.nsdl.beckn.common.constant.ApplicationConstant.BLANK;
import static com.nsdl.beckn.common.constant.ApplicationConstant.PIPE;
import static com.nsdl.beckn.common.enums.OndcUserTypeNew.BUYER_APP;
import static com.nsdl.beckn.common.enums.OndcUserTypeNew.SELLER_APP;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.deleteWhitespace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nsdl.beckn.api.model.lookup.KeyPair;
import com.nsdl.beckn.api.model.lookup.LookupResponse;
import com.nsdl.beckn.api.model.lookup.NetworkParticipant;
import com.nsdl.beckn.api.model.lookup.SellerOnRecord;
import com.nsdl.beckn.common.model.ActiveSellerCacheModel;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HelperService {

	@Value("${beckn.parameter.participant-url-protocol-type:https}")
	private String protocolType;

	private static final String DOUBLE_SLASH = "://";

	public List<SellerOnRecord> findSellerOnRecord(NetworkParticipant participant, String domain, String subscriberId) {

		List<SellerOnRecord> sellerList = new ArrayList<>();

		if (SELLER_APP.type().equalsIgnoreCase(participant.getType()) && isTrue(participant.getMsn())
				&& domain.equalsIgnoreCase(participant.getDomain())) {

			List<SellerOnRecord> recordList = participant.getSellerOnRecord();

			if (isNotEmpty(recordList)) {
				sellerList.addAll(recordList);
			} else {
				log.error("even though seller {} is msn, it does not have any seller_on_record", subscriberId);
			}
		}

		log.info("size of seller on record is {} for subscriber_id {}", sellerList.size(), subscriberId);
		return sellerList;
	}

	public List<String> findBuyers(LookupResponse response, String domain) {

		List<String> buyerList = new ArrayList<>();

		List<NetworkParticipant> participantList = response.getNetworkParticipant();

		if (isNotEmpty(participantList)) {
			for (NetworkParticipant participant : participantList) {

				if (BUYER_APP.type().equalsIgnoreCase(participant.getType()) && domain.equalsIgnoreCase(participant.getDomain())) {
					buyerList.add(participant.getSubscriberUrl());

					if (buyerList.size() > 1) {
						log.error("wrong data received from registry. cannot have more than 1 buyers in the same domain[{}] for subscriber_id {}", participant
								.getDomain(), response.getSubscriberId());
						log.error("error lookup response received is {}", response);
					}
				}
			}
		}

		log.info("size of buyer list is {} for subscriber_id {}", buyerList.size(), response.getSubscriberId());
		return buyerList;
	}

	public List<String> findParticipantCities(LookupResponse response) {

		List<String> cityList = new ArrayList<>();

		List<NetworkParticipant> participantList = response.getNetworkParticipant();

		if (isNotEmpty(participantList)) {
			for (NetworkParticipant participant : participantList) {

				if (SELLER_APP.type().equalsIgnoreCase(participant.getType())) {

					if (isTrue(participant.getMsn())) {

						List<SellerOnRecord> recordList = participant.getSellerOnRecord();

						if (isNotEmpty(recordList)) {
							for (SellerOnRecord seller : recordList) {
								if (isNotEmpty(seller.getCityCode())) {
									cityList.addAll(seller.getCityCode());
								}
							}
						}
					} else {
						cityList.addAll(participant.getCityCode());
					}
				}
			}
		}

		return cityList;
	}

	public boolean isParticipantSeller(LookupResponse response) {

		List<NetworkParticipant> participantList = response.getNetworkParticipant();

		if (isNotEmpty(participantList)) {
			for (NetworkParticipant participant : participantList) {

				if (SELLER_APP.type().equalsIgnoreCase(participant.getType())) {
					return true;
				}
			}
		}

		return false;
	}

	public boolean isParticipantInDomainAndSeller(ActiveSellerCacheModel model, String domain) {

		if (domain.equalsIgnoreCase(model.getDomain())) {
			return true;
		}

		return false;
	}

	public boolean matchKeyId(String keyId, LookupResponse response) {
		String subscriberId = response.getSubscriberId();

		List<NetworkParticipant> participantList = response.getNetworkParticipant();

		if (isNotEmpty(participantList)) {
			for (NetworkParticipant participant : participantList) {

				if (SELLER_APP.type().equalsIgnoreCase(participant.getType()) && isTrue(participant.getMsn())) {

					List<SellerOnRecord> sellerOnRecords = participant.getSellerOnRecord();

					if (isNotEmpty(sellerOnRecords)) {
						for (SellerOnRecord record : sellerOnRecords) {
							if ((subscriberId + PIPE + record.getUniqueKeyId()).equalsIgnoreCase(keyId)) {
								return true;
							}
						}
					} else {
						log.warn("even subscriber {} is msn, there is no seller_on_record");
					}
				} else if ((subscriberId + PIPE + BLANK).equalsIgnoreCase(keyId)) {
					return true;
				}
			}
		}

		return false;
	}

	public Map<String, String> getUniqueUrls(List<ActiveSellerCacheModel> modelList) {
		Map<String, String> map = new HashMap<>();

		for (ActiveSellerCacheModel model : modelList) {

			String subId = model.getSubscriberId().trim();

			String url = deleteWhitespace(this.protocolType + DOUBLE_SLASH + subId + model.getSubscriberUrl());
			if (map.containsKey(url)) {
				log.warn("duplicate url [{}]. no search request will be send to this seller url", url);
				continue;
			}
			map.put(url, subId);

		}

		return map;
	}

	public boolean checkIfBuyerOrNonMsnSeller(LookupResponse response, String domain) {

		List<NetworkParticipant> participantList = response.getNetworkParticipant();

		if (isNotEmpty(participantList)) {
			for (NetworkParticipant participant : participantList) {

				String type = participant.getType();
				if (domain.equalsIgnoreCase(participant.getDomain()) && (BUYER_APP.type().equalsIgnoreCase(type) || SELLER_APP.type().equalsIgnoreCase(type)
						&& isFalse(participant.getMsn()))) {
					return true;
				}
			}
		}
		return false;
	}

	public String findPublicKey(LookupResponse response, String uniqueKeyId) {
		log.info("going to find the signing_public_key for unique_key_id[{}] received in the header", uniqueKeyId);

		if (response == null) {
			log.error("LookupResponse received is null, so returning signing_public_key as null");
			return null;
		}

		String subscriberId = response.getSubscriberId();

		if (uniqueKeyId.equalsIgnoreCase(response.getUniqueKeyId())) {
			return response.getSigningPublicKey();
		}

		List<NetworkParticipant> participantList = response.getNetworkParticipant();

		if (isNotEmpty(participantList)) {
			for (NetworkParticipant participant : participantList) {

				if (SELLER_APP.type().equalsIgnoreCase(participant.getType()) && isTrue(participant.getMsn())) {

					List<SellerOnRecord> sellerOnRecords = participant.getSellerOnRecord();

					if (isNotEmpty(sellerOnRecords)) {
						for (SellerOnRecord record : sellerOnRecords) {
							if (uniqueKeyId.equalsIgnoreCase(record.getUniqueKeyId())) {
								KeyPair keyPair = record.getKeyPair();
								if (keyPair != null) {
									return keyPair.getSigningPublicKey();

								}
							}
						}
					} else {
						log.error("even subscriber {} is msn, there is no seller_on_record. so null signing_public_key will be returned", subscriberId);
					}
				}
			}
		} else {
			log.error("network_participant list is empty for subscriber {}", subscriberId);
		}

		return null;
	}
}
