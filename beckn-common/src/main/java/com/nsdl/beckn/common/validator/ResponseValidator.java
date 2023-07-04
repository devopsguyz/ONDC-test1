package com.nsdl.beckn.common.validator;

import static com.nsdl.beckn.common.enums.AuditType.ACK_BY_BUYER;
import static com.nsdl.beckn.common.enums.AuditType.ACK_BY_GATEWAY;
import static com.nsdl.beckn.common.enums.AuditType.ACK_BY_SELLER;
import static com.nsdl.beckn.common.enums.AuditType.INVALID_RESPONSE_BY_BUYER;
import static com.nsdl.beckn.common.enums.AuditType.INVALID_RESPONSE_BY_GATEWAY;
import static com.nsdl.beckn.common.enums.AuditType.INVALID_RESPONSE_BY_SELLER;
import static com.nsdl.beckn.common.enums.AuditType.NACK_BY_BUYER;
import static com.nsdl.beckn.common.enums.AuditType.NACK_BY_GATEWAY;
import static com.nsdl.beckn.common.enums.AuditType.NACK_BY_SELLER;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nsdl.beckn.api.enums.AckStatus;
import com.nsdl.beckn.api.model.common.Context;
import com.nsdl.beckn.api.model.response.ResponseAck;
import com.nsdl.beckn.api.model.response.ResponseMessage;
import com.nsdl.beckn.common.model.AckNackResponseModel;
import com.nsdl.beckn.common.util.JsonUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ResponseValidator {

	@Autowired
	private JsonUtil jsonUtil;

	public AckNackResponseModel checkBuyerResponse(String response, Context ctx) {
		log.warn("response by buyer[{}] for txid {}, msgid {} is {}", ctx.getBapId(), ctx.getTransactionId(), ctx.getMessageId(), response);
		String responseEnum = null;
		AckNackResponseModel model = new AckNackResponseModel();
		try {
			ResponseAck res = this.jsonUtil.toModel(response, ResponseAck.class);
			ResponseMessage message = res.getMessage();
			if (message != null && message.getAck() != null) {
				AckStatus status = message.getAck().getStatus();
				if (AckStatus.ACK.equals(status)) {
					responseEnum = ACK_BY_BUYER.name();
					model.setAck(true);
				} else if (AckStatus.NACK.equals(status)) {
					responseEnum = NACK_BY_BUYER.name();
					model.setResponse(this.jsonUtil.toJsonOrDefault(res.getError()));
				} else {
					responseEnum = INVALID_RESPONSE_BY_BUYER.name();
					model.setResponse(response);
				}
			} else {
				log.error("either message or ack object is null in buyer response");
				responseEnum = INVALID_RESPONSE_BY_BUYER.name();
				model.setResponse(response);
			}

		} catch (Exception e) {
			log.error("error while casting to buyer respone", e);
			responseEnum = INVALID_RESPONSE_BY_BUYER.name();
			model.setResponse(response);
		}

		model.setResponseEnum(responseEnum);
		log.info("buyer[{}] ack/nack response model is: {}", ctx.getBapId(), model);
		return model;
	}

	public AckNackResponseModel checkSellerResponse(String response, Context ctx) {
		log.info("response by seller[{}] for txid {}, msgid {} is {}", ctx.getBppId(), ctx.getTransactionId(), ctx.getMessageId(), response);
		String responseEnum = null;
		AckNackResponseModel model = new AckNackResponseModel();
		try {
			ResponseAck res = this.jsonUtil.toModel(response, ResponseAck.class);
			ResponseMessage message = res.getMessage();
			if (message != null && message.getAck() != null) {
				AckStatus status = message.getAck().getStatus();
				if (AckStatus.ACK.equals(status)) {
					responseEnum = ACK_BY_SELLER.name();
					model.setAck(true);
				} else if (AckStatus.NACK.equals(status)) {
					responseEnum = NACK_BY_SELLER.name();
					model.setResponse(this.jsonUtil.toJsonOrDefault(res.getError()));
				} else {
					responseEnum = INVALID_RESPONSE_BY_SELLER.name();
					model.setResponse(response);
				}
			} else {
				log.error("either message or ack object is null in seller response");
				responseEnum = INVALID_RESPONSE_BY_SELLER.name();
				model.setResponse(response);
			}

		} catch (Exception e) {
			log.error("error while casting to seller respone", e);
			responseEnum = INVALID_RESPONSE_BY_SELLER.name();
			model.setResponse(response);
		}

		model.setResponseEnum(responseEnum);
		log.info("seller[{}] ack/nack response model is: {}", ctx.getBppId(), model);
		return model;
	}

	public AckNackResponseModel checkGatewayResponse(String response, Context ctx) {
		log.info("response by gateway for txid {}, msgid {} is {}", ctx.getTransactionId(), ctx.getMessageId(), response);
		String responseEnum = null;
		AckNackResponseModel model = new AckNackResponseModel();
		try {
			ResponseAck res = this.jsonUtil.toModel(response, ResponseAck.class);
			ResponseMessage message = res.getMessage();
			if (message != null && message.getAck() != null) {
				AckStatus status = message.getAck().getStatus();
				if (AckStatus.ACK.equals(status)) {
					responseEnum = ACK_BY_GATEWAY.name();
					model.setAck(true);
				} else if (AckStatus.NACK.equals(status)) {
					responseEnum = NACK_BY_GATEWAY.name();
					model.setResponse(this.jsonUtil.toJsonOrDefault(res.getError()));
				} else {
					responseEnum = INVALID_RESPONSE_BY_GATEWAY.name();
					model.setResponse(response);
				}
			} else {
				log.error("either message or ack object is null in gateway response");
				responseEnum = INVALID_RESPONSE_BY_GATEWAY.name();
				model.setResponse(response);
			}

		} catch (Exception e) {
			log.error("error while casting to gateway respone model", e);
			responseEnum = INVALID_RESPONSE_BY_GATEWAY.name();
			model.setResponse(response);
		}

		model.setResponseEnum(responseEnum);
		log.info("gateway ack/nack response model is: {}", model);
		return model;
	}

}
