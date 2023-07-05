package com.nsdl.beckn.adaptor.controller;

import static com.nsdl.beckn.adaptor.AdaptorConstant.CONTEXT_ROOT_BUYER;
import static com.nsdl.beckn.api.enums.ContextAction.ON_SEARCH;
import static com.nsdl.beckn.common.constant.ApplicationConstant.BLANK;
import static com.nsdl.beckn.common.constant.ApplicationConstant.ON;
import static com.nsdl.beckn.common.constant.ApplicationConstant.REMOTE_HOST;
import static com.nsdl.beckn.common.enums.AuditType.NACK_TO_BUYER;
import static com.nsdl.beckn.common.enums.AuditType.REQUEST_BY_BUYER;
import static com.nsdl.beckn.common.enums.AuditType.RESPONSE_BY_GATEWAY;
import static com.nsdl.beckn.common.enums.AuditType.RESPONSE_BY_SELLER;
import static com.nsdl.beckn.common.enums.OndcUserType.BUYER;
import static com.nsdl.beckn.common.exception.ErrorCode.AUTH_FAILED;
import static com.nsdl.beckn.common.exception.ErrorCode.UNKNOWN_ERROR;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.time.Instant;
import java.time.LocalDateTime;

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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nsdl.beckn.adaptor.ApiClassFinder;
import com.nsdl.beckn.adaptor.service.buyer.BuyerApiService;
import com.nsdl.beckn.adaptor.service.buyer.BuyerOnApiService;
import com.nsdl.beckn.api.model.common.Context;
import com.nsdl.beckn.api.model.object.ObjectRequest;
import com.nsdl.beckn.common.builder.ResponseBuilder;
import com.nsdl.beckn.common.exception.ApplicationException;
import com.nsdl.beckn.common.exception.ErrorCode;
import com.nsdl.beckn.common.model.AuditFlagModel;
import com.nsdl.beckn.common.model.AuditModel;
import com.nsdl.beckn.common.service.AuditService;
import com.nsdl.beckn.common.service.CommonService;
import com.nsdl.beckn.common.util.JsonUtil;
import com.nsdl.beckn.common.validator.BodyValidator;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class BuyerController {

	@Autowired
	private BuyerApiService apiService;

	@Autowired
	private BuyerOnApiService onApiService;

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
	private ObjectMapper mapper;

	@Autowired
	private BodyValidator bodyValidator;

	@Value("${beckn.parameter.validate-schema: true}")
	private boolean validateSchema;

	@Value("${beckn.blip.enabled: false}")
	private boolean blipEnabled;

	@PostMapping(value = {CONTEXT_ROOT_BUYER + "/{api}", CONTEXT_ROOT_BUYER + "/{urlId}/{api}"})
	public ResponseEntity<String> search(@PathVariable String api, @PathVariable(required = false) String urlId, @RequestHeader HttpHeaders headers,
			@RequestBody String body) throws JsonProcessingException {

		final Instant start = Instant.now();
		String remoteHost = headers.get(REMOTE_HOST).get(0);

		log.info("in buyer api[{}] with urlId[{}] from remote host[{}]", api, urlId, remoteHost);
		if (!ON_SEARCH.value().equalsIgnoreCase(api)) {
			log.info("The body in buyer api[{}] is {}", api, body);
		}

		Context ctx = new Context();
		String blipMessage = BLANK;
		boolean callback = false;

		try {
			ObjectRequest objectModel = this.jsonUtil.toModel(body, ObjectRequest.class);
			ctx = objectModel.getContext();

			String action = ctx.getAction();
			callback = action.startsWith(ON);

			if (isNotBlank(urlId)) {
				this.commonService.validateUrlIdAndSubscriberId(urlId, ctx.getBapId(), true);
			}

			if (this.blipEnabled) {
				blipMessage = objectModel.getMessage().toString();
			}

			if (this.validateSchema) {
				Class<?> schemaClass = this.classFinder.findClass(ctx);
				log.info("incoming request will be validated against schema {}", schemaClass);
				this.jsonUtil.validateSchema(body, schemaClass);
			} else {
				log.debug("schema validation is off");
			}

			if (!callback) {
				body = this.mapper.readValue(body, JsonNode.class).toString();
				log.info("{} minified body is {}", api, body);
			}

			// do the audit
			audit(start, api, headers, body, ctx, blipMessage, callback);

			// validate the fields of context if present
			this.bodyValidator.validateRequestBody(ctx,  api, BUYER.type());

			if (callback) {
				log.info("in buyer callback for action {}", action);
				headers.remove(headers.HOST);
				return this.onApiService.callback(api, headers, body, ctx, start);
			}

			ResponseEntity<String> response = this.apiService.doPost(api, headers, body, ctx, start);

			log.info("for api {} returning the ack to buyer {} for txId {} & msgId {}", api, ctx.getBapId(), ctx.getTransactionId(), ctx.getMessageId());
			return response;

		} catch (Exception e) {
			log.error("error for api[{}] in buyer controller", api, e);

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
				.type(findErrorType(api))
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
		if ("on_search".equalsIgnoreCase(action)) {
			return RESPONSE_BY_GATEWAY.name();
		}
		return isCallback ? RESPONSE_BY_SELLER.name() : REQUEST_BY_BUYER.name();
	}

	private String findErrorType(String action) {
		return NACK_TO_BUYER.name();
	}

}
