package com.nsdl.beckn.adaptor.service.buyer;

import static com.nsdl.beckn.api.enums.ContextAction.SEARCH;
import static com.nsdl.beckn.common.constant.ApplicationConstant.*;
import static com.nsdl.beckn.common.enums.AuditType.ACK_BY_SELLER;
import static com.nsdl.beckn.common.enums.AuditType.BLOCKED_SELLER;
import static com.nsdl.beckn.common.enums.AuditType.ERROR_CALLING_GATEWAY;
import static com.nsdl.beckn.common.enums.AuditType.ERROR_CALLING_SELLER;
import static com.nsdl.beckn.common.enums.AuditType.REQUEST_TO_SELLER;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.nsdl.beckn.api.model.common.Context;
import com.nsdl.beckn.common.enums.AuditType;
import com.nsdl.beckn.common.model.AuditFlagModel;
import com.nsdl.beckn.common.model.AuditModel;
import com.nsdl.beckn.common.model.SubscriberApiModel;
import com.nsdl.beckn.common.sender.Sender;
import com.nsdl.beckn.common.service.AuditService;
import com.nsdl.beckn.common.service.CommonService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SellerCaller {

	@Autowired
	private Sender sender;

	@Autowired
	private AuditService auditService;

	@Autowired
	private CommonService commonService;

	@Value("${beckn.gateway.url}")
	private String gatewayUrl;

	@Async(value = "httpExecutor")
	public void callSeller(String api, HttpHeaders headers, String json, Context ctx, SubscriberApiModel apiModel) {
		Instant start = Instant.now();

		String url = ctx.getBppUri();
		String sellerId = ctx.getBppId();
		String source = "seller";

		boolean isGatway = false;
		if (SEARCH.name().equalsIgnoreCase(ctx.getAction())) {
			url = this.gatewayUrl + "/" + api;
			log.warn("calling gateway search");
			isGatway = true;
			source = "gateway";
		} else {
			if (RECON_STATUS.equalsIgnoreCase(ctx.getAction())||
					PREPARE_FOR_SETTLE.equalsIgnoreCase(ctx.getAction()) ||
					COLLECTOR_RECON.equalsIgnoreCase(ctx.getAction())) {
				url = apiModel.getEndpoint();
			} else {
				url = url + "/" + api;
			}
		}

		try {
			if (isNotBlockedUrl(ctx.getBapId(), url)) {
				log.info("sending request to {} at url {}", source, url);

				// do audit before
				AuditModel fields = AuditModel.builder()
						.type(isGatway ? AuditType.REQUEST_TO_GATEWAY.name() : REQUEST_TO_SELLER.name())
						.headers(headers).body(json).context(ctx)
						.startTime(start)
						.endTime(Instant.now())
						.createdOn(LocalDateTime.now())
						.blipMsg(json)
						.auditFlags(AuditFlagModel.builder().blip(true).database(true).build())
						.build();

				this.auditService.audit(fields);
				// end of audit

				String response = this.sender.send(url, headers, json, apiModel);

				// do audit after
				AuditModel fieldsAfter = AuditModel.builder()
						.type(isGatway ? AuditType.ACK_BY_GATEWAY.name() : ACK_BY_SELLER.name())
						.headers(headers).body(json).context(ctx)
						.startTime(start)
						.endTime(Instant.now())
						.createdOn(LocalDateTime.now())
						.blipMsg(response)
						.auditFlags(AuditFlagModel.builder().blip(true).database(true).build())
						.build();

				this.auditService.audit(fieldsAfter);
				// end of audit

				log.warn("from {}[{}], ack for {} received for txId {} and msgId {}", source, sellerId, api, ctx.getTransactionId(), ctx.getMessageId());
			} else {
				String blockedMsg = "seller url [" + url + "] is part of blacklist. no http call will be made";
				log.error(blockedMsg);

				// do audit
				AuditModel errorFields = AuditModel.builder()
						.type(BLOCKED_SELLER.name())
						.headers(headers).body(json).context(ctx)
						.startTime(start)
						.endTime(Instant.now())
						.createdOn(LocalDateTime.now())
						.blipMsg(blockedMsg)
						.errorTechnical(blockedMsg)
						.errorStackTrace(blockedMsg)
						.auditFlags(AuditFlagModel.builder().blip(true).database(true).build())
						.build();

				this.auditService.audit(errorFields);
			}
		} catch (Exception e) {
			log.error("error while sending post request to {}. error is :", source, e);

			// do audit
			AuditModel fieldsAfter = AuditModel.builder()
					.type(isGatway ? ERROR_CALLING_GATEWAY.name() : ERROR_CALLING_SELLER.name())
					.headers(headers).body(json).context(ctx)
					.startTime(start)
					.endTime(Instant.now())
					.createdOn(LocalDateTime.now())
					.blipMsg(this.commonService.getErrorMessage(e))
					.errorTechnical(this.commonService.getErrorCauseOrMessage(e))
					.errorStackTrace(this.commonService.getStackTrace(e))
					.auditFlags(AuditFlagModel.builder().blip(true).database(true).build())
					.build();

			this.auditService.audit(fieldsAfter);
			// end of audit
		}

	}

	private boolean isNotBlockedUrl(String bapId, String url) {
		List<String> sellerBacklist = this.commonService.getBlockedSellers();

		for (String blockedUrl : sellerBacklist) {
			if (isNotBlank(blockedUrl) && isNotBlank(url) && url.trim().toLowerCase().contains(blockedUrl.trim().toLowerCase())) {
				return false;
			}
		}
		return true;
	}

}
