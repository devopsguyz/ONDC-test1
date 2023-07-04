package com.nsdl.beckn.common.service;

import static com.nsdl.beckn.common.constant.ApplicationConstant.*;
import static com.nsdl.beckn.common.exception.ErrorCode.API_NOT_FOUND;
import static com.nsdl.beckn.common.exception.ErrorCode.NO_SUBSCRIBER_CONFIGURED;
import static com.nsdl.beckn.common.exception.ErrorCode.SELLER_ON_RECORD_NOT_FOUND;
import static com.nsdl.beckn.common.exception.ErrorCode.SUBSCRIBER_NOT_FOUND;

import java.text.MessageFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nsdl.beckn.common.exception.ApplicationException;
import com.nsdl.beckn.common.model.SellerOnRecordModel;
import com.nsdl.beckn.common.model.SubscriberApiModel;
import com.nsdl.beckn.common.model.SubscriberModel;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SubscriberService {

	@Autowired
	public CacheService service;

	public SubscriberModel getActiveFirstSubscriber() {
		List<SubscriberModel> list = this.service.loadSubscriberList();

		SubscriberModel model = list
				.stream()
				.findFirst()
				.orElseThrow(() -> {
					String error = "no subscriber configured in the database";
					throw new ApplicationException(NO_SUBSCRIBER_CONFIGURED, error);
				});

		log.info("first subscriber loaded is {}", model);
		return model;
	}

	public SubscriberModel getSubscriberById(String subscriberId) {
		List<SubscriberModel> list = this.service.loadSubscriberList();

		SubscriberModel model = list
				.stream()
				.filter(entity -> entity.getSubscriberId().equalsIgnoreCase(subscriberId))
				.findFirst()
				.orElseThrow(() -> {
					String error = "not able to find the subscriberId [" + subscriberId + "] in the database";
					throw new ApplicationException(SUBSCRIBER_NOT_FOUND, error);
				});

		log.info("for subscriberId[{}] matched model is {}", subscriberId, model);
		return model;
	}

	public SubscriberModel getSubscriberByShortName(String shortName) {
		List<SubscriberModel> list = this.service.loadSubscriberList();

		SubscriberModel model = list
				.stream()
				.filter(entity -> entity.getShortName().equalsIgnoreCase(shortName))
				.findFirst()
				.orElseThrow(() -> {
					String error = "not able to find the shortName [" + shortName + "] in the database";
					throw new ApplicationException(SUBSCRIBER_NOT_FOUND, error);
				});

		log.info("for shortName[{}] matched model is {}", shortName, model);
		return model;
	}

	public SubscriberApiModel getSubscriberApi(String subscriberId, String apiName) {
		apiName = apiName.trim().toLowerCase();
		String action;

		if (apiName.equalsIgnoreCase(ON_RECON_STATUS) || apiName.equalsIgnoreCase(ON_RECEIVER_RECON) ||
				apiName.equalsIgnoreCase(ON_COLLECTOR_RECON)) {
			action = apiName;
		} else {
			action = apiName.startsWith(ON) ? apiName.replace(ON, BLANK) : apiName;
		}

		List<SubscriberApiModel> list = this.service.loadSubscriberApiList();

		SubscriberApiModel model = list
				.stream()
				.filter(entity -> entity.getSubscriberId().equalsIgnoreCase(subscriberId) && entity.getName().equalsIgnoreCase(action))
				.findFirst()
				.orElseThrow(() -> {
					String error = MessageFormat.format("not able to find api[{0}] for the subscriberId [{1}] in the database", action, subscriberId);
					throw new ApplicationException(API_NOT_FOUND, error);
				});

		log.info("for subscriberId[{}] matched model is {}", subscriberId, model);
		return model;
	}

	public SellerOnRecordModel getSellerOnRecord(String subscriberId, String providerId) {

		List<SellerOnRecordModel> list = this.service.loadSubscriberSellerOnRecordList(subscriberId);

		SellerOnRecordModel sellerOnRecordModel = list
				.stream()
				.filter(entity -> providerId.equalsIgnoreCase(entity.getProviderId()))
				.findFirst()
				.orElseThrow(() -> {
					String error = MessageFormat.format("not able to find seller_on_record for subscriberId[{0}] & providerId[{1}] in the database",
							subscriberId, providerId);
					log.error(error);
					throw new ApplicationException(SELLER_ON_RECORD_NOT_FOUND, error);
				});

		log.info("for subscriberId[{}] & providerId[{}] sellerOnRecordModel is {}", subscriberId, providerId, sellerOnRecordModel);
		return sellerOnRecordModel;
	}
}
