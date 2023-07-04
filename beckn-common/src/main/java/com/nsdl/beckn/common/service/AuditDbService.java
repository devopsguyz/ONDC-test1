package com.nsdl.beckn.common.service;

import static com.nsdl.beckn.common.enums.DomainType.MOBILITY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nsdl.beckn.api.model.common.Context;
import com.nsdl.beckn.common.dao.AlertConfigDao;
import com.nsdl.beckn.common.dao.AuditDao;
import com.nsdl.beckn.common.entity.AlertConfigEntity;
import com.nsdl.beckn.common.entity.ApiAuditEntity;
import com.nsdl.beckn.common.entity.ApiAuditErrorEntity;
import com.nsdl.beckn.common.model.AuditModel;
import com.nsdl.beckn.common.model.JsonRequestModel;
import com.nsdl.beckn.common.model.JsonResponseModel;
import com.nsdl.beckn.common.util.AdaptorUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class AuditDbService {

	@Autowired
	private AuditDao dao;

	@Autowired
	private CommonService commonService;

	@Autowired
	private AdaptorUtil adaptorUtil;

	@Autowired
	private AlertService alertService;

	@Autowired
	private AlertConfigDao alertConfigDao;

	@Value("${beckn.entity.domain:}")
	private String domain;

	@Value("${beckn.alert.enabled}")
	private boolean alertEnabled;

	@Async(value = "dbExecutor")
	public void databaseAudit(boolean isDbFlagEnabled, AuditModel auditModel, String type, Instant endTime, LocalDateTime createdOn) {
		Context ctx = auditModel.getContext();
		log.info("In db audit for case {} for api {} with txId {} & msgId {}", type, ctx.getAction(), ctx.getTransactionId(), ctx
				.getMessageId());

		boolean isDbConfigured = this.adaptorUtil.isDataBasePersistanceConfigured();

		if (isDbFlagEnabled && isDbConfigured) {
			try {
				dbAudit(auditModel, type, endTime, createdOn);
			} catch (Exception e) {
				log.error("exception while saving api audit logs in table", e);
			}
		}
	}

	public void dbAudit(AuditModel auditModel, String type, Instant endTime, LocalDateTime createdOn) {
		Context ctx = auditModel.getContext();

		long timeTaken = this.commonService.getTimeDiff(auditModel.getStartTime(), endTime);

		ApiAuditEntity auditEntity = new ApiAuditEntity();

		String id = UUID.randomUUID().toString();
		auditEntity.setId(id);
		auditEntity.setMessageId(ctx.getMessageId());
		auditEntity.setRemoteHost(this.commonService.getRemoteHost(auditModel.getHeaders()));
		auditEntity.setTransactionId(ctx.getTransactionId());
		auditEntity.setBuyerId(ctx.getBapId());
		auditEntity.setSellerId(ctx.getBppId());
		auditEntity.setAction(ctx.getAction());
		auditEntity.setDomain(ctx.getDomain());
		auditEntity.setCity(ctx.getCity());
		auditEntity.setCoreVersion(ctx.getCoreVersion());
		auditEntity.setJson(auditModel.getBody());
		auditEntity.setStatus("N");
		auditEntity.setType(convertType(type));
		auditEntity.setHeaders(auditModel.getHeaders().toString());
		auditEntity.setErrorStackTrace(auditModel.getErrorStackTrace());
		auditEntity.setErrorTechnical(auditModel.getErrorTechnical());
		auditEntity.setErrorFunctional(auditModel.getErrorFunctional());
		auditEntity.setCreatedOn(checkCreatedOn(createdOn, type, ctx.getTransactionId()));
		auditEntity.setTimeTaken(String.valueOf(timeTaken));
		auditEntity.setHostId(this.commonService.getHostId());

		if (this.alertEnabled) {
			sendAlert(auditModel);
		}

		this.dao.saveApiAudit(auditEntity);
		log.info("AuditEntity saved in database with id {}", id);
	}

	private LocalDateTime checkCreatedOn(LocalDateTime createdOn, String type, String txId) {
		if (createdOn == null) {
			log.error("createdOn is null for type {} and txId {}", type, txId);
			return LocalDateTime.now();
		}
		return createdOn;
	}

	public void auditSchemaError(Class<?> schemaClass, String body, String error, LocalDateTime createdOn) {
		log.info("In auditSchemaError....");

		ApiAuditErrorEntity entity = new ApiAuditErrorEntity();
		String id = UUID.randomUUID().toString();

		log.info("Id for schema error audit: {}", id);

		entity.setId(id);
		entity.setSchemaClass(schemaClass.getSimpleName());
		entity.setJson(body);
		entity.setError(error);
		entity.setCreatedOn(createdOn);

		log.info("going to save ApiAuditEntity");
		this.dao.saveSchemaError(entity);
	}

	@Transactional
	public String health() {
		try {
			BigInteger health = this.dao.health();
			log.info("health count in table is {}", health);
			if (health.intValue() > 0) {
				return "OK";
			}
		} catch (Exception e) {
			log.error("error while determining health {}", e);
		}
		return "NOK";
	}

	public List<JsonResponseModel> getApiResponse(JsonRequestModel model) {

		if (isBlank(model.getTransactionId())) {
			log.warn("no txid found in the request. so empty result will be returned");
			return new ArrayList<>();
		}

		List<Object[]> list = this.dao.getAuditData(model);
		log.info("size of json list for txid {} is {}", model.getTransactionId(), list.size());

		List<JsonResponseModel> modelList = buildResponseModelList(list);

		return modelList;

	}

	private List<JsonResponseModel> buildResponseModelList(List<Object[]> list) {
		return list
				.stream()
				.map(this::buildJsonResponseModel)
				.sorted(Comparator.comparing(JsonResponseModel::getCreatedOn))
				.collect(Collectors.toList());

	}

	private JsonResponseModel buildJsonResponseModel(Object[] fields) {
		LocalDateTime createdOn = (LocalDateTime) fields[1];
		long timeLapsed = Duration.between(createdOn, LocalDateTime.now()).toSeconds();

		return JsonResponseModel.builder()
				.json((String) fields[0])
				.createdOn(createdOn)
				.receivedOn(createdOn.toString())
				.timeSinceReceived(timeLapsed).build();
	}

	private String convertType(String type) {
		if (MOBILITY.type().equalsIgnoreCase(this.domain) && isNotBlank(type)) {
			if (type.contains("BUYER")) {
				return type.replace("BUYER", "BAP");
			}
			if (type.contains("SELLER")) {
				return type.replace("SELLER", "BPP");
			}
		}
		return type;
	}

	public List<JsonResponseModel> getApiPagedResponse(JsonRequestModel model) {
		return null;
	}

	public void sendAlert(AuditModel auditModel) {
		String error = auditModel.getErrorStackTrace();

		if (StringUtils.isNoneBlank(error)) {
			List<AlertConfigEntity> list = this.alertConfigDao.getAlertConfig();
			for (AlertConfigEntity configEntity : list) {
				if (error.contains(configEntity.getPattern())) {
					this.alertService.sendAlert(auditModel, configEntity);
				}

			}
		}
	}
}
