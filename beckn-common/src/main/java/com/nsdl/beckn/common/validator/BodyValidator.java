package com.nsdl.beckn.common.validator;

import static org.apache.commons.lang3.StringUtils.isBlank;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nsdl.beckn.api.model.common.Context;
import com.nsdl.beckn.common.exception.ApplicationException;
import com.nsdl.beckn.common.exception.ErrorCode;
import com.nsdl.beckn.common.exception.ErrorCodeOndc;
import com.nsdl.beckn.common.exception.OndcException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BodyValidator {

	@Value("${beckn.entity.type}")
	private String entityType;

	public void validateRequestBody(Context ctx, String api, String expectedEntityType) throws JsonProcessingException {

		log.debug("Entity type is {}", this.entityType);

		if (!expectedEntityType.equalsIgnoreCase(this.entityType)) {
			throw new ApplicationException(ErrorCode.INVALID_ENTITY_TYPE);
		}

		if (ctx == null) {
			log.error("context is null in the request", ctx);
			throw new ApplicationException(ErrorCode.INVALID_REQUEST);
		}

		if (isBlank(ctx.getDomain())) {
			log.error("domain not present in the context {}", ctx);
			throw new ApplicationException(ErrorCode.INVALID_DOMAIN);
		}

		if (isBlank(ctx.getCity())) {
			log.error("city not present in the context {}", ctx);
			throw new ApplicationException(ErrorCode.INVALID_CITY);
		}

		if (ctx.getTransactionId() == null) {
			log.error("transaction id not found in the context");
			throw new ApplicationException(ErrorCode.TRANSACTION_ID_NOT_PRESENT);
		}

		if (!api.equalsIgnoreCase(ctx.getAction())) {
			log.error("action not matching with api call");
			throw new ApplicationException(ErrorCode.INVALID_ACTION);
		}

		log.info("body validation of the request is ok");
	}

	public void validateGatewayRequestBody(Context ctx, String api, String expectedEntityType) throws JsonProcessingException {

		log.info("Entity type is {}", this.entityType);
		if (!expectedEntityType.equalsIgnoreCase(this.entityType)) {
			throw new ApplicationException(ErrorCode.INVALID_ENTITY_TYPE);
		}

		if (ctx == null) {
			log.error("context is null in the request", ctx);
			throw new OndcException(ErrorCodeOndc.GATEWAY_10000);
		}

		if (isBlank(ctx.getDomain())) {
			log.error("domain not present in the context {}", ctx);
			throw new OndcException(ErrorCodeOndc.GATEWAY_10000);
		}

		if (isBlank(ctx.getCity())) {
			log.error("city not present in the context {}", ctx);
			throw new OndcException(ErrorCodeOndc.GATEWAY_10002);
		}

		if (ctx.getTransactionId() == null) {
			log.error("transaction id not found in the context");
			throw new OndcException(ErrorCodeOndc.GATEWAY_10000);
		}

		if (!api.equalsIgnoreCase(ctx.getAction())) {
			log.error("action not matching with api call");
			throw new OndcException(ErrorCodeOndc.GATEWAY_10000);
		}

		log.info("body validation of the request is ok");
	}

}
