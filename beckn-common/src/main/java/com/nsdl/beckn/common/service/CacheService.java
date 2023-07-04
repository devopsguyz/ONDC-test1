package com.nsdl.beckn.common.service;

import static com.nsdl.beckn.common.constant.ApplicationConstant.BECKN_API_COMMON_CACHE;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.nsdl.beckn.common.dao.ParameterDao;
import com.nsdl.beckn.common.dao.SubscriberDao;
import com.nsdl.beckn.common.entity.ParameterEntity;
import com.nsdl.beckn.common.entity.SellerOnRecordEntity;
import com.nsdl.beckn.common.entity.SubscriberApiEntity;
import com.nsdl.beckn.common.entity.SubscriberEntity;
import com.nsdl.beckn.common.model.SellerOnRecordModel;
import com.nsdl.beckn.common.model.SubscriberApiModel;
import com.nsdl.beckn.common.model.SubscriberModel;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CacheService {

	@Autowired
	public SubscriberDao dao;

	@Autowired
	private ParameterDao parameterDao;

	@Cacheable(value = BECKN_API_COMMON_CACHE)
	public List<SubscriberModel> loadSubscriberList() {
		log.info("going to load all subscriber");

		List<SubscriberEntity> list = this.dao.getSubscriberList();
		List<SubscriberModel> modelList = list.stream().map(this::buildSubscriberModel).collect(Collectors.toList());

		log.info("count of active subscribers {}", modelList.size());
		return modelList;
	}

	@Cacheable(value = BECKN_API_COMMON_CACHE)
	public List<SubscriberApiModel> loadSubscriberApiList() {
		log.info("going to load all subscriber's api");

		List<SubscriberApiEntity> list = this.dao.getSubscriberApiList();
		log.info("SubscriberApiEntity list is {}", list);
		List<SubscriberApiModel> modelList = list.stream().map(this::buildSubscriberApiModel).collect(Collectors.toList());

		log.info("count of subscribers api {}", modelList.size());
		return modelList;
	}

	@Cacheable(value = BECKN_API_COMMON_CACHE)
	public List<SellerOnRecordModel> loadSubscriberSellerOnRecordList(String subscriberId) {
		log.info("going to load all subscriber's seller on records");

		List<SellerOnRecordEntity> list = this.dao.getSellerOnRecordEntityList();
		List<SellerOnRecordModel> modelList = list.stream()
				.filter(entity -> subscriberId.equalsIgnoreCase(entity.getPk().getSubscriberId()))
				.map(this::buildSellerOnRecordModel)
				.collect(Collectors.toList());

		log.info("count of seller_on_records of subscriber[{}] is {}", subscriberId, modelList.size());
		return modelList;
	}

	@Cacheable(value = BECKN_API_COMMON_CACHE)
	public List<ParameterEntity> getParameterList() { return this.parameterDao.getParameterList(); }

	private SubscriberModel buildSubscriberModel(SubscriberEntity entity) {
		SubscriberModel model = new SubscriberModel();
		model.setSubscriberId(entity.getSubscriberId());
		model.setMsn(entity.isMsn());
		model.setShortName(entity.getShortName());
		model.setUniqueKeyId(entity.getUniqueKeyId());
		model.setPrivateKey(entity.getPrivateKey());
		model.setSubscriberIp(entity.getSubscriberIp());
		return model;
	}

	private SubscriberApiModel buildSubscriberApiModel(SubscriberApiEntity entity) {
		SubscriberApiModel model = new SubscriberApiModel();
		model.setSubscriberId(entity.getPk().getSubscriberId());
		model.setName(entity.getPk().getName());
		model.setValidateAuthHeader(entity.isValidateAuthHeader());
		model.setSetAuthHeader(entity.isSetAuthHeader());
		model.setEndpoint(entity.getEndpoint());
		model.setTimeout(entity.getTimeout());
		model.setRetryCount(entity.getRetryCount());
		model.setHeaderValidity(entity.getHeaderValidity());
		return model;
	}

	private SellerOnRecordModel buildSellerOnRecordModel(SellerOnRecordEntity entity) {
		SellerOnRecordModel model = new SellerOnRecordModel();
		model.setSubscriberId(entity.getPk().getSubscriberId());
		model.setProviderId(entity.getProviderId());
		model.setUniqueKeyId(entity.getPk().getUniqueKeyId());
		model.setPrivateKey(entity.getPrivateKey());
		return model;
	}
}
