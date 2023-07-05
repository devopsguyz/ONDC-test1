package com.nsdl.beckn.adaptor.service.seller;

import static com.nsdl.beckn.common.constant.ApplicationConstant.COLLECTOR_RECON;
import static com.nsdl.beckn.common.enums.AuditType.ERROR_CALLING_SELLER;
import static com.nsdl.beckn.common.enums.AuditType.REQUEST_TO_SELLER;

import java.time.Instant;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.nsdl.beckn.api.model.common.Context;
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
public class SellerInternalCaller {

	@Autowired
	private AuditService auditService;

	@Autowired
	private CommonService commonService;

	@Autowired
	private ResponseValidator responseValidator;

	@Autowired
	private SubscriberService subscriberService;

	@Autowired
	private Sender sender;

	@Async(value = "commonExecutor")
	public void callSellerInternalApi(Instant start, HttpHeaders headers, String json, Context ctx, SubscriberModel configModel) {
		SubscriberApiModel apiModel = this.subscriberService.getSubscriberApi(configModel.getSubscriberId(), ctx.getAction());

		String url = "";
		// Condition to check if the action are RSP actions
		// If yes then get the RSP endpoint from the BPP uri and postfix the action endpoint
		if (COLLECTOR_RECON.equalsIgnoreCase(ctx.getAction())) {
			url = ctx.getBppUri() + "/" + ctx.getAction();
		} else {
			url = apiModel.getEndpoint();
		}
		log.info("going to call seller internal api at {} \nwith payload : \n {}", url, json);

		try {
			// do audit before
			auditBefore(headers, json, start, ctx);

			String response = this.sender.send(url, headers, json, apiModel);
			log.info("response received from calling seller internal api {}", response);

			// do audit after
			auditAfter(headers, json, start, ctx, response);

		} catch (Exception e) {
			log.error("error while sending post request to seller[{}] at its internal url {}", configModel.getSubscriberId(), url, e);
			// do audit
			auditError(headers, json, start, ctx, e);
		}
	}

	private void auditBefore(HttpHeaders headers, String json, Instant start, Context ctxCopy) {
		AuditModel fields = AuditModel.builder()
				.type(REQUEST_TO_SELLER.name())
				.headers(headers).body(json).context(ctxCopy)
				.startTime(start)
				.endTime(Instant.now())
				.createdOn(LocalDateTime.now())
				.blipMsg(json)
				.auditFlags(AuditFlagModel.builder().blip(true).database(true).build())
				.build();

		this.auditService.audit(fields);
	}

	private void auditAfter(HttpHeaders headers, String json, Instant start, Context ctxCopy, String response) {
		AckNackResponseModel ackNackResponseModel = this.responseValidator.checkSellerResponse(response, ctxCopy);
		AuditModel fieldsAfter = AuditModel.builder()
				.type(ackNackResponseModel.getResponseEnum())
				.headers(headers).body(json).context(ctxCopy)
				.startTime(start)
				.endTime(Instant.now())
				.createdOn(LocalDateTime.now())
				.errorFunctional(ackNackResponseModel.getResponse())
				.blipMsg(response)
				.auditFlags(AuditFlagModel.builder().blip(true).database(true).build())
				.build();

		this.auditService.audit(fieldsAfter);
	}

	private void auditError(HttpHeaders headers, String json, Instant start, Context ctxCopy, Exception e) {
		AuditModel fields = AuditModel.builder()
				.type(ERROR_CALLING_SELLER.name())
				.headers(headers).body(json).context(ctxCopy)
				.startTime(start)
				.endTime(Instant.now())
				.createdOn(LocalDateTime.now())
				.errorTechnical(this.commonService.getErrorCauseOrMessage(e))
				.errorStackTrace(this.commonService.getStackTrace(e))
				.blipMsg(this.commonService.getErrorMessage(e))
				.auditFlags(AuditFlagModel.builder().blip(true).database(true).build())
				.build();

		this.auditService.audit(fields);
	}

}
