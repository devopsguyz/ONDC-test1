package com.nsdl.beckn.adaptor.service.buyer;

import static com.nsdl.beckn.common.constant.ApplicationConstant.*;
import static com.nsdl.beckn.common.enums.AuditType.ACK_TO_BUYER;

import java.time.Instant;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nsdl.beckn.api.model.common.Context;
import com.nsdl.beckn.common.builder.HeaderBuilder;
import com.nsdl.beckn.common.builder.ResponseBuilder;
import com.nsdl.beckn.common.model.AuditFlagModel;
import com.nsdl.beckn.common.model.AuditModel;
import com.nsdl.beckn.common.model.SubscriberApiModel;
import com.nsdl.beckn.common.model.SubscriberModel;
import com.nsdl.beckn.common.service.AuditService;
import com.nsdl.beckn.common.service.CommonService;
import com.nsdl.beckn.common.service.SubscriberService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BuyerApiService {

	@Autowired
	private ResponseBuilder responseBuilder;

	@Autowired
	private HeaderBuilder authHeaderBuilder;

	@Autowired
	private SellerCaller sellerCaller;

	@Autowired
	private AuditService auditService;

	@Autowired
	private SubscriberService subscriberService;

	@Autowired
	private CommonService commonService;

	public ResponseEntity<String> doPost(String api, HttpHeaders requestHeaders, String body, Context ctx, Instant start) throws Exception {
		log.info("Going to validate host-ip/json request before sending to seller");

		ResponseEntity<String> successResponse = this.responseBuilder.buildResponseEntity(ctx);

		String configId = ctx.getBapId();
		log.info("Checking if the action is collector_recon or receiver_recon");
		if (COLLECTOR_RECON.equalsIgnoreCase(ctx.getAction()) ||
				RECEIVER_RECON.equalsIgnoreCase(ctx.getAction())) {
			configId = ctx.getBppId();
		}

		SubscriberModel subscriberModel = this.subscriberService.getSubscriberById(configId);
		SubscriberApiModel apiModel = this.subscriberService.getSubscriberApi(configId, api);

		// validate remote host ip's
		this.commonService.validateRemoteHostIPs(subscriberModel, requestHeaders.get(REMOTE_HOST).get(0));

		log.info("configuration found for subscriberid: {} and api: {}", configId, api);

		// set auth header
		HttpHeaders headers = this.authHeaderBuilder.buildHeaders(body, null, subscriberModel, apiModel);
		log.info("auth header build completed");

		this.sellerCaller.callSeller(api, headers, body, ctx, apiModel);

		// do audit
		AuditModel fields = AuditModel.builder()
				.type(ACK_TO_BUYER.name())
				.headers(headers).body(body).context(ctx)
				.startTime(start)
				.endTime(Instant.now())
				.createdOn(LocalDateTime.now())
				.blipMsg(successResponse.toString())
				.auditFlags(AuditFlagModel.builder().blip(true).database(true).build())
				.build();

		this.auditService.audit(fields);
		// end of audit

		return successResponse;
	}

}
