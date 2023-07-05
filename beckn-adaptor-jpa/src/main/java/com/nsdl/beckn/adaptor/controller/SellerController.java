package com.nsdl.beckn.adaptor.controller;

import static com.nsdl.beckn.adaptor.AdaptorConstant.CONTEXT_ROOT_SELLER;
import static com.nsdl.beckn.api.enums.ContextAction.ON_SEARCH;
import static com.nsdl.beckn.api.enums.ContextAction.SEARCH;
import static com.nsdl.beckn.common.constant.ApplicationConstant.BLANK;
import static com.nsdl.beckn.common.constant.ApplicationConstant.ON;
import static com.nsdl.beckn.common.constant.ApplicationConstant.REMOTE_HOST;
import static com.nsdl.beckn.common.enums.AuditType.NACK_TO_BUYER;
import static com.nsdl.beckn.common.enums.AuditType.NACK_TO_GATEWAY;
import static com.nsdl.beckn.common.enums.AuditType.NACK_TO_SELLER;
import static com.nsdl.beckn.common.enums.AuditType.REQUEST_BY_BUYER;
import static com.nsdl.beckn.common.enums.AuditType.REQUEST_BY_GATEWAY;
import static com.nsdl.beckn.common.enums.AuditType.RESPONSE_BY_SELLER;
import static com.nsdl.beckn.common.enums.OndcUserType.SELLER;
import static com.nsdl.beckn.common.exception.ErrorCode.AUTH_FAILED;
import static com.nsdl.beckn.common.exception.ErrorCode.UNKNOWN_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.time.Instant;
import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nsdl.beckn.adaptor.ApiClassFinder;
import com.nsdl.beckn.adaptor.service.seller.SellerApiService;
import com.nsdl.beckn.adaptor.service.seller.SellerOnApiService;
import com.nsdl.beckn.api.model.common.Context;
import com.nsdl.beckn.api.model.object.ObjectRequest;
import com.nsdl.beckn.common.builder.ResponseBuilder;
import com.nsdl.beckn.common.exception.ApplicationException;
import com.nsdl.beckn.common.exception.ErrorCode;
import com.nsdl.beckn.common.exception.ErrorCodeOndc;
import com.nsdl.beckn.common.exception.OndcException;
import com.nsdl.beckn.common.model.AuditFlagModel;
import com.nsdl.beckn.common.model.AuditModel;
import com.nsdl.beckn.common.model.SubscriberModel;
import com.nsdl.beckn.common.service.AuditService;
import com.nsdl.beckn.common.service.CommonService;
import com.nsdl.beckn.common.service.SubscriberService;
import com.nsdl.beckn.common.util.JsonUtil;
import com.nsdl.beckn.common.validator.BodyValidator;
import com.nsdl.beckn.common.validator.HeaderValidator;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class SellerController {

	@Autowired
	private SellerApiService apiService;

	@Autowired
	private SellerOnApiService onApiService;

	@Autowired
	private CommonService commonService;

	@Autowired
	private JsonUtil jsonUtil;

	@Autowired
	private ApiClassFinder classFinder;

	@Autowired
	private ResponseBuilder responseBuilder;

	@Autowired
	private AuditService auditService;

	@Autowired
	private BodyValidator bodyValidator;

	@Autowired
	private HeaderValidator validator;

	@Autowired
	private SubscriberService subscriberService;

	@Value("${beckn.parameter.validate-schema: true}")
	private boolean validateSchema;

	@Value("${beckn.blip.enabled: false}")
	private boolean blipEnabled;

	// @PostMapping(CONTEXT_ROOT_SELLER + "/{api}")
	@PostMapping(CONTEXT_ROOT_SELLER + "/{urlId}" + "/{api}")
	public ResponseEntity<String> search(@PathVariable String api, @PathVariable String urlId, @RequestHeader HttpHeaders headers, @RequestBody String body,
			HttpServletRequest servletRequest) throws JsonProcessingException {

		final Instant start = Instant.now();

		String requestUrl = servletRequest.getRequestURL().toString();
		log.info("call for api[{}] has come from remote-host[{}] & urlId[{}] & request url: {}", api, headers.get(REMOTE_HOST).get(0), urlId, requestUrl);

		if (!ON_SEARCH.value().equalsIgnoreCase(api)) {
			log.info("The body in seller api[{}] is {}", api, body);
		}

		Context ctx = new Context();
		String blipMessage = BLANK;
		boolean callback = false;

		try {
			ObjectRequest objectModel = this.jsonUtil.toModel(body, ObjectRequest.class);
			ctx = objectModel.getContext();

			String action = ctx.getAction();
			callback = action.trim().startsWith(ON);

			this.commonService.validateUrlIdAndSubscriberId(urlId, ctx.getBppId(), !SEARCH.value().equals(ctx.getAction()));

			if (this.blipEnabled) {
				blipMessage = objectModel.getMessage().toString();
			}

			if (this.validateSchema) {
				Class<?> schemaClass = this.classFinder.findClass(ctx);
				log.info("incoming request will be validated against schema {}", schemaClass);
				this.jsonUtil.validateSchema(body, schemaClass);
			} else {
				log.debug("schema validation is off on seller side");
			}

			// do the audit
			audit(start, api, headers, body, ctx, blipMessage, callback);

			// validate the fields of context
			this.bodyValidator.validateRequestBody(ctx, api, SELLER.type());

			if (callback) {
				log.info("in seller callback for action {}", action);
				return this.onApiService.doPost(headers, body, ctx, start, urlId);
			}

			SubscriberModel subscriberModel = validateSignature(api, urlId, headers, body, ctx);

			ResponseEntity<String> response = this.apiService.doPost(start, api, headers, body, ctx, subscriberModel);

			log.info("for api {} returning the ack to buyer {} for txId {} & msgId {}", api, ctx.getBapId(), ctx.getTransactionId(), ctx.getMessageId());
			return response;

		} catch (Exception e) {
			log.error("error for api[{}] in seller controller", api, e);

			HttpStatus httpStatus = null;
			ErrorCode errorCode = null;
			String errorMsg = this.commonService.getErrorMessage(e);

			if (e instanceof ApplicationException) {
				ApplicationException gex = (ApplicationException) e;
				errorCode = gex.getErrorCode();
				httpStatus = AUTH_FAILED == gex.getErrorCode() ? UNAUTHORIZED : OK;
			} else {
				httpStatus = OK;
				errorCode = UNKNOWN_ERROR;
			}

			String response = this.responseBuilder.buildNotAckResponseAdaptor(ctx, errorCode, errorMsg);

			// audit error
			auditError(start, api, headers, body, ctx, e, callback);

			return new ResponseEntity<>(response, httpStatus);
		}
	}

	private SubscriberModel validateSignature(String api, String urlId, HttpHeaders headers, String body, Context ctx) throws Exception {
		String bapId = ctx.getBapId();
		String bppId = null;
		SubscriberModel subscriberModel = null;

		if (SEARCH.value().equalsIgnoreCase(ctx.getAction())) {
			subscriberModel = this.subscriberService.getSubscriberByShortName(urlId);
			String subscriberId = subscriberModel.getSubscriberId();
			bppId = subscriberId;
			log.info("starting the process for subscriberId {}", subscriberId);
		} else {
			bppId = ctx.getBppId();
			subscriberModel = this.subscriberService.getSubscriberById(bppId);

			// validateUrlIdAndSubscriberId(urlId, subscriberModel.getSubscriberId());
		}

		boolean authenticate = this.subscriberService.getSubscriberApi(bppId, ctx.getAction()).isValidateAuthHeader();

		log.info("does entity[{}] requires to be authenticated ? {}", bapId, authenticate);
		if (authenticate) {
			try {
				this.validator.validateHeader(bppId, headers, body, ctx.getDomain(), ctx.getCity());
			} catch (Exception ex) {
				log.error("buyer header validation failed", ex);
				throw new OndcException(ErrorCodeOndc.SELLER_30016);
			}
		}

		return subscriberModel;
	}

	private void audit(Instant start, String api, HttpHeaders headers, String body, Context ctx, String blipMessage, boolean isCallback) {
		String type = findType(api, isCallback);

		AuditModel fields = AuditModel.builder()
				.type(type)
				.headers(headers).body(body).context(ctx)
				.startTime(start)
				.endTime(Instant.now())
				.createdOn(LocalDateTime.now())
				.blipMsg(blipMessage)
				.auditFlags(AuditFlagModel.builder().blip(true).database(true).file(true).build())
				.build();

		this.auditService.audit(fields);
	}

	private void auditError(Instant start, String api, HttpHeaders headers, String body, Context ctx, Throwable e, boolean isCallback) {
		AuditModel fields = AuditModel.builder()
				.type(findErrorType(api, isCallback))
				.headers(headers).body(body).context(ctx)
				.startTime(start)
				.endTime(Instant.now())
				.createdOn(LocalDateTime.now())
				.errorTechnical(this.commonService.getErrorCauseOrMessage(e))
				.errorStackTrace(this.commonService.getStackTrace(e))
				.auditFlags(AuditFlagModel.builder().database(true).build())
				.build();

		this.auditService.audit(fields);
	}

	private String findType(String action, boolean isCallback) {
		if (SEARCH.value().equalsIgnoreCase(action)) {
			return REQUEST_BY_GATEWAY.name();
		}
		return isCallback ? RESPONSE_BY_SELLER.name() : REQUEST_BY_BUYER.name();
	}

	private String findErrorType(String action, boolean isCallback) {
		if (isCallback) {
			return NACK_TO_SELLER.name();
		}
		if (SEARCH.value().equalsIgnoreCase(action)) {
			return NACK_TO_GATEWAY.name();
		}
		return NACK_TO_BUYER.name();
	}

}
