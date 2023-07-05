package com.nsdl.beckn.adaptor.service.seller;

import static com.nsdl.beckn.api.enums.ContextAction.SEARCH;
import static com.nsdl.beckn.common.enums.AuditType.ACK_TO_BUYER;
import static com.nsdl.beckn.common.enums.AuditType.ACK_TO_GATEWAY;

import java.time.Instant;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nsdl.beckn.api.model.common.Context;
import com.nsdl.beckn.common.builder.ResponseBuilder;
import com.nsdl.beckn.common.model.AuditFlagModel;
import com.nsdl.beckn.common.model.AuditModel;
import com.nsdl.beckn.common.model.SubscriberModel;
import com.nsdl.beckn.common.service.AuditService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SellerApiService {

	@Autowired
	private ResponseBuilder responseBuilder;

	@Autowired
	private SellerInternalCaller sellerCaller;

	@Autowired
	private AuditService auditService;

	public ResponseEntity<String> doPost(Instant start, String api, HttpHeaders headers, String body, Context ctx, SubscriberModel configModel)
			throws Exception {
		log.info("Going to validate json request before sending to seller internal api {}", api);

		ResponseEntity<String> successResponse = this.responseBuilder.buildResponseEntity(ctx);

		log.info("{} is made with bapid {}", api, ctx.getBapId());

		this.sellerCaller.callSellerInternalApi(start, new HttpHeaders(), body, ctx, configModel);

		// do audit
		AuditModel fields = AuditModel.builder()
				.type(findType(ctx.getAction()))
				.headers(headers).body(body).context(ctx)
				.startTime(start)
				.endTime(Instant.now())
				.createdOn(LocalDateTime.now())
				.blipMsg(successResponse.toString())
				.auditFlags(AuditFlagModel.builder().database(true).build())
				.build();

		this.auditService.audit(fields);
		// end of audit

		return successResponse;
	}

	private String findType(String action) {
		if (SEARCH.name().equalsIgnoreCase(action)) {
			return ACK_TO_GATEWAY.name();
		}
		return ACK_TO_BUYER.name();
	}

}
