package com.nsdl.beckn.common.service;

import static com.nsdl.beckn.common.constant.ApplicationConstant.SLASH;
import static com.nsdl.beckn.common.enums.AuditType.BLOCKED_BUYER;
import static com.nsdl.beckn.common.enums.AuditType.ERROR_CALLING_BUYER;
import static com.nsdl.beckn.common.enums.AuditType.RESPONSE_TO_BUYER;
import static com.nsdl.beckn.common.enums.OndcUserType.BUYER;
import static com.nsdl.beckn.common.enums.OndcUserType.GATEWAY;
import static com.nsdl.beckn.common.enums.OndcUserType.SELLER;
import static com.nsdl.beckn.common.exception.ErrorCode.BLOCKED_BUYER_ERROR;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.nsdl.beckn.api.model.common.Context;
import com.nsdl.beckn.common.builder.ModelBuilder;
import com.nsdl.beckn.common.exception.ApplicationException;
import com.nsdl.beckn.common.exception.ErrorCode;
import com.nsdl.beckn.common.model.AckNackResponseModel;
import com.nsdl.beckn.common.model.AuditModel;
import com.nsdl.beckn.common.model.BlipBodyModel;
import com.nsdl.beckn.common.model.BlipModel;
import com.nsdl.beckn.common.model.SubscriberApiModel;
import com.nsdl.beckn.common.sender.Sender;
import com.nsdl.beckn.common.validator.ResponseValidator;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuditHttpService {

	@Autowired
	private Sender sender;

	@Autowired
	private SubscriberService subscriberService;

	@Autowired
	private AuditDbService auditDbService;

	@Autowired
	private BlipService blipService;

	@Autowired
	private ModelBuilder modelBuilder;

	@Autowired
	private CommonService commonService;

	@Autowired
	private ResponseValidator responseValidator;

	@Value("${beckn.entity.type}")
	private String entityType;

	@Value("${beckn.entity.id:}")
	private String entityId;

	@Value("${beckn.blip.enabled: false}")
	private boolean blipEnabled;

	@Value("${beckn.blip.url:}")
	private String loggingUrl;

	@Value("${beckn.gateway.url:}")
	private String gatewayUrl;

	@Async(value = "httpExecutor")
	public void http(AuditModel auditModel) {
		log.info("http callback is about to happen");
		try {
			String action = auditModel.getContext().getAction();
			String subscriberId = findSubscriberId(auditModel.getContext());

			SubscriberApiModel apiModel = this.subscriberService.getSubscriberApi(subscriberId, action);
			String url = apiModel.getEndpoint();

			if (!GATEWAY.type().equalsIgnoreCase(this.entityType) && StringUtils.isBlank(url)) {
				log.warn("no http endpoint found in the config json file for subscriberId {} & action {}", subscriberId, action);
				return;
			}

			// blip call before
			buildAndCallBlip(auditModel, auditModel.getBody());
			this.auditDbService.databaseAudit(true, auditModel, RESPONSE_TO_BUYER.name(), Instant.now(), LocalDateTime.now());

			// make http call
			String response = doPost(auditModel, apiModel);
			AckNackResponseModel ackNackResponseModel = this.responseValidator.checkBuyerResponse(response, auditModel.getContext());
			auditModel.setErrorFunctional(ackNackResponseModel.getResponse());
			this.auditDbService.databaseAudit(true, auditModel, ackNackResponseModel.getResponseEnum(), Instant.now(), LocalDateTime.now());

			// blip call after
			buildAndCallBlip(auditModel, response);
		} catch (Throwable e) {
			String errorType = null;
			log.error("exception while sending http call", e);

			if (e instanceof ApplicationException) {
				ApplicationException appEx = (ApplicationException) e;
				if (BLOCKED_BUYER_ERROR.getCode() == appEx.getErrorCode().getCode()) {
					log.info("case-1: setting error enum as {}", BLOCKED_BUYER.name());
					errorType = BLOCKED_BUYER.name();
				} else {
					log.info("case-2: setting error enum as {}", ERROR_CALLING_BUYER.name());
					errorType = ERROR_CALLING_BUYER.name();
				}
			} else {
				log.info("case-3: setting error enum as {}", ERROR_CALLING_BUYER.name());
				errorType = ERROR_CALLING_BUYER.name();
			}

			auditModel.setErrorTechnical(this.commonService.getErrorCauseOrMessage(e));
			auditModel.setErrorStackTrace(this.commonService.getStackTrace(e));
			this.auditDbService.databaseAudit(true, auditModel, errorType, Instant.now(), LocalDateTime.now());

			// blip call to report error
			buildAndCallBlip(auditModel, this.commonService.getErrorMessage(e));
		}

	}

	private String doPost(AuditModel auditModel, SubscriberApiModel apiModel) {
		log.info("in doPost of AuditHttpService with matchedApi {}", apiModel);

		if (GATEWAY.type().equalsIgnoreCase(this.entityType)) {

			Context context = auditModel.getContext();

			String baseUrl = context.getBapUri();

			List<String> buyerBacklist = this.commonService.getBlockedBuyers();

			for (String blockedUrl : buyerBacklist) {
				if (isNotBlank(blockedUrl) && isNotBlank(baseUrl) && baseUrl.trim().toLowerCase().contains(blockedUrl.trim().toLowerCase())) {
					String error = "no http call will be made to buyer as url [" + baseUrl + "] is a blacklisted";
					log.error(error);
					throw new ApplicationException(ErrorCode.BLOCKED_BUYER_ERROR, error);
				}
			}

			String url = baseUrl + SLASH + context.getAction();
			log.info("sending the http response from gateway to buyer at url {}", url);

			String reponse = this.sender.send(url.trim(), auditModel.getHeaders(), auditModel.getBody(), apiModel);

			return reponse;
		}

		// send the response back to real buyer api if configured
		String baseUrl = apiModel.getEndpoint();
		if (isNotBlank(baseUrl)) {
			String url = baseUrl + SLASH + auditModel.getContext().getAction();
			log.info("calling internal api at url {}", url);
			String response = this.sender.send(url.trim(), auditModel.getHeaders(), auditModel.getBody(), apiModel);
			return response;
		}

		return null;

	}

	private String findSubscriberId(Context context) {
		if (GATEWAY.type().equalsIgnoreCase(this.entityType)) {
			return this.entityId;
		}
		if (BUYER.type().equalsIgnoreCase(this.entityType)) {
			return context.getBapId().trim();
		}
		if (SELLER.type().equalsIgnoreCase(this.entityType)) {
			return context.getBppId().trim();
		}
		return null;
	}

	private void buildAndCallBlip(AuditModel auditModel, String body) {
		// blip call
		if (this.blipEnabled) {
			try {
				BlipBodyModel bodyModel = new BlipBodyModel();
				bodyModel.setJson(body);

				BlipModel blipModel = this.modelBuilder.buildBlipModel(auditModel);
				blipModel.setMessage(bodyModel);

				this.blipService.blip(blipModel, auditModel.getType());
			} catch (Exception e) {
				log.error("error in buildAndCallBlip: {}", e);
			}
		} else {
			log.info("blip audit service is disabled");
		}
	}

	public String doPostBlip(String json) {
		if (this.blipEnabled) {
			log.debug("making the http post call to ondc logging url {}", this.loggingUrl);
			String response = this.sender.send(this.loggingUrl, new HttpHeaders(), json, null);
			return response;
		}
		log.info("blip audit service is disabled");
		return null;

	}

}
