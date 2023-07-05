package com.nsdl.beckn.adaptor.service.seller;

import static com.nsdl.beckn.api.enums.ContextAction.ON_SEARCH;
import static com.nsdl.beckn.common.constant.ApplicationConstant.*;
import static com.nsdl.beckn.common.enums.AuditType.ACK_TO_SELLER;
import static com.nsdl.beckn.common.enums.AuditType.NACK_TO_SELLER;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nsdl.beckn.api.model.common.Context;
import com.nsdl.beckn.common.builder.HeaderBuilder;
import com.nsdl.beckn.common.builder.ResponseBuilder;
import com.nsdl.beckn.common.exception.ErrorCode;
import com.nsdl.beckn.common.model.AckNackResponseModel;
import com.nsdl.beckn.common.model.AuditFlagModel;
import com.nsdl.beckn.common.model.AuditModel;
import com.nsdl.beckn.common.model.SubscriberApiModel;
import com.nsdl.beckn.common.model.SubscriberModel;
import com.nsdl.beckn.common.sender.Sender;
import com.nsdl.beckn.common.service.AuditService;
import com.nsdl.beckn.common.service.CommonService;
import com.nsdl.beckn.common.service.SubscriberService;
import com.nsdl.beckn.common.validator.ResponseValidator;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SellerOnApiService {

	@Autowired
	private ResponseBuilder responseBuilder;

	@Autowired
	private HeaderBuilder authHeaderBuilder;

	@Autowired
	private AuditService auditService;

	@Autowired
	private SubscriberService subscriberService;

	@Autowired
	private Sender sender;

	@Autowired
	private ResponseValidator responseValidator;

	@Autowired
	private CommonService commonService;

	@Value("${beckn.gateway.url}")
	private String gatewayUrl;

	public ResponseEntity<String> doPost(HttpHeaders requestHeaders, String body, Context ctx, Instant start, String urlId) throws Exception {
		String api = ctx.getAction();
		log.info("Going to validate json request before sending to seller for api {}", api);

		ResponseEntity<String> responseToSeller = this.responseBuilder.buildResponseEntity(ctx);

		String bppId = ctx.getBppId();
		SubscriberModel subscriberModel = null;
		SubscriberApiModel apiModel = null;

		if (ON_RECON_STATUS.equalsIgnoreCase(api) || ON_RECEIVER_RECON.equalsIgnoreCase(api) ||
				ON_COLLECTOR_RECON.equalsIgnoreCase(api)) {
			subscriberModel = this.subscriberService.getSubscriberById(urlId);
			apiModel = this.subscriberService.getSubscriberApi(urlId, api);
		} else {
			subscriberModel = this.subscriberService.getSubscriberById(bppId);
			apiModel = this.subscriberService.getSubscriberApi(bppId, api);
		}

		log.info("configuration found for subscriberid: {} and api: {}", bppId, api);

		// validate remote host ip's
		this.commonService.validateRemoteHostIPs(subscriberModel, requestHeaders.get(REMOTE_HOST).get(0));

		// set auth header

		HttpHeaders headers = this.authHeaderBuilder.buildHeaders(body, getProviderId(requestHeaders), subscriberModel, apiModel);
		log.info("auth header build completed as {}", headers);

		// do audit-before
		AuditModel fields = AuditModel.builder()
				.type(this.commonService.findSuccessAuditType(ctx.getAction()))
				.headers(headers).body(body).context(ctx)
				.startTime(start)
				.endTime(Instant.now())
				.createdOn(LocalDateTime.now())
				.blipMsg(body)
				.auditFlags(AuditFlagModel.builder().database(true).build())
				.build();

		this.auditService.audit(fields);
		// end of audit-before

		String url = buildUrl(ctx, apiModel);
		boolean isError = false;
		boolean isOnSearch = ON_SEARCH.name().equalsIgnoreCase(ctx.getAction());
		try {
			String response = this.sender.send(url, headers, body, apiModel);

			// do audit-before
			AckNackResponseModel ackNackResponseModel = this.responseValidator.checkBuyerResponse(response, ctx);
			AckNackResponseModel ackNackResponseModelGateway = this.responseValidator.checkGatewayResponse(response, ctx);
			AuditModel fieldsAfter = AuditModel.builder()
					.type(isOnSearch ? ackNackResponseModelGateway.getResponseEnum() : ackNackResponseModel.getResponseEnum())
					.headers(headers).body(body).context(ctx)
					.startTime(start)
					.endTime(Instant.now())
					.createdOn(LocalDateTime.now())
					.errorFunctional(isOnSearch ? ackNackResponseModelGateway.getResponse() : ackNackResponseModel.getResponse())
					.blipMsg(body)
					.auditFlags(AuditFlagModel.builder().database(true).build())
					.build();

			this.auditService.audit(fieldsAfter);
			// end of audit-before

		} catch (Exception e) {
			ErrorCode errorEnum = ErrorCode.ADAPTOR_ERROR_CALLING_BUYER;
			if (isOnSearch) {
				errorEnum = ErrorCode.ADAPTOR_ERROR_CALLING_GATEWAY;
			}

			isError = true;
			String errorResponse = this.responseBuilder.buildNotAckResponseAdaptor(ctx, errorEnum, errorEnum.getMessage());
			responseToSeller = new ResponseEntity<>(errorResponse, HttpStatus.OK);
			log.error("error while sending post request to url {}", url);
			log.error("error while sending post :", e);

			// do audit for exception
			AuditModel fieldsAfter = AuditModel.builder()
					.type(this.commonService.findErrorAuditType(ctx.getAction()))
					.headers(headers).body(body).context(ctx)
					.startTime(start)
					.endTime(Instant.now())
					.createdOn(LocalDateTime.now())
					.blipMsg(this.commonService.getErrorMessage(e))
					.errorTechnical(this.commonService.getErrorCauseOrMessage(e))
					.errorStackTrace(this.commonService.getStackTrace(e))
					.auditFlags(AuditFlagModel.builder().database(true).build())
					.build();

			this.auditService.audit(fieldsAfter);
			// end of audit for exception
		}

		if (isError) {
			// audit nack to seller
			auditAckNackToSeller(start, headers, body, ctx, responseToSeller.getBody(), NACK_TO_SELLER.name());
		} else {
			// audit ack to seller
			auditAckNackToSeller(start, headers, body, ctx, responseToSeller.getBody(), ACK_TO_SELLER.name());
		}
		return responseToSeller;
	}

	private String getProviderId(HttpHeaders requestHeaders) {
		List<String> list = requestHeaders.get(PROVIDER_ID);
		if (isEmpty(list)) {
			return null;
		}
		return list.get(0);
	}

	private String buildUrl(Context context, SubscriberApiModel apiModel) {
		if (ON_SEARCH.name().equalsIgnoreCase(context.getAction())) {
			return this.gatewayUrl + SLASH + context.getAction();
		}
		if (ON_COLLECTOR_RECON.equalsIgnoreCase(context.getAction()) ||
				ON_RECEIVER_RECON.equalsIgnoreCase(context.getAction()) ||
				ON_RECON_STATUS.equalsIgnoreCase(context.getAction())) {
			return apiModel.getEndpoint();
		}
		return context.getBapUri() + SLASH + context.getAction();
	}

	private void auditAckNackToSeller(Instant start, HttpHeaders headers, String body, Context ctx, String response, String type) {
		// do audit
		AuditModel fields = AuditModel.builder()
				.type(type)
				.headers(headers).body(body).context(ctx)
				.startTime(start)
				.endTime(Instant.now())
				.createdOn(LocalDateTime.now())
				.blipMsg(response)
				.auditFlags(AuditFlagModel.builder().blip(true).database(true).build())
				.build();

		this.auditService.audit(fields);
	}

}
