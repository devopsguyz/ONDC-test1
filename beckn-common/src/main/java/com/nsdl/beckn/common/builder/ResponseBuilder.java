package com.nsdl.beckn.common.builder;

import static com.nsdl.beckn.common.constant.ApplicationConstant.SIGN_ALGORITHM;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nsdl.beckn.api.enums.AckStatus;
import com.nsdl.beckn.api.model.common.Ack;
import com.nsdl.beckn.api.model.common.Context;
import com.nsdl.beckn.api.model.common.Error;
import com.nsdl.beckn.api.model.response.Response;
import com.nsdl.beckn.api.model.response.ResponseMessage;
import com.nsdl.beckn.common.exception.ApplicationException;
import com.nsdl.beckn.common.exception.ErrorCode;
import com.nsdl.beckn.common.exception.ErrorCodeOndc;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ResponseBuilder {

	@Autowired
	private ObjectMapper mapper;

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);

	public ResponseEntity<String> buildResponseEntity(Context ctx)
			throws JsonProcessingException {
		String response = buildAckResponse(ctx);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		return ResponseEntity.ok()
				.headers(headers)
				.body(response);
	}

	private String buildAckResponse(Context context) throws JsonProcessingException {
		Response response = new Response();
		ResponseMessage resMsg = new ResponseMessage();

		resMsg.setAck(new Ack(AckStatus.ACK));
		response.setMessage(resMsg);

		context.setTimestamp(FORMATTER.format(LocalDateTime.now(ZoneOffset.UTC)));

		response.setContext(context);
		return this.mapper.writeValueAsString(response);
	}

	public String buildNotAckResponseAdaptor(Context ctx, ErrorCode errorEnum, String errorMsg) throws JsonProcessingException {

		Response response = new Response();
		ResponseMessage resMsg = new ResponseMessage();

		resMsg.setAck(new Ack(AckStatus.NACK));
		response.setMessage(resMsg);

		Error e = new Error();
		e.setType("Adaptor");
		e.setCode(String.valueOf(errorEnum.getCode()));
		e.setMessage(errorMsg);
		response.setError(e);

		ctx.setTimestamp(FORMATTER.format(LocalDateTime.now(ZoneOffset.UTC)));
		response.setContext(ctx);
		String body = this.mapper.writeValueAsString(response);

		return body;
	}

	public String buildNotAckResponse(Context ctx, ErrorCodeOndc errorEnum, String errorMsg) throws JsonProcessingException {

		Response response = new Response();
		ResponseMessage resMsg = new ResponseMessage();

		resMsg.setAck(new Ack(AckStatus.NACK));
		response.setMessage(resMsg);

		Error e = new Error();
		e.setType(errorEnum.getType());
		e.setCode(errorEnum.getCode());
		e.setMessage(errorMsg);
		response.setError(e);

		ctx.setTimestamp(FORMATTER.format(LocalDateTime.now(ZoneOffset.UTC)));
		response.setContext(ctx);
		String body = this.mapper.writeValueAsString(response);

		return body;
	}

	public String buildErrorResponse(ApplicationException ae) throws JsonProcessingException {

		Response response = new Response();

		ResponseMessage message = new ResponseMessage();
		Error error = new Error();

		Ack ack = new Ack();
		ack.setStatus(AckStatus.NACK);

		message.setAck(ack);

		response.setMessage(message);

		error.setCode(ae.getErrorCode().toString());
		error.setMessage(ae.getMessage());

		response.setError(error);

		ObjectMapper mapper = new ObjectMapper();

		String header = "(created) (expires) digest";
		HttpHeaders headers = new HttpHeaders();
		headers.set("WWW-Authenticate", "Signature realm=\"" + SIGN_ALGORITHM + "\",headers=\"" + header + "\"");

		return mapper.writeValueAsString(response);

	}

	public ResponseEntity buildErrorResponse1() {

		String errorMessage = "error has occured";

		Response response = new Response();

		Ack ack = new Ack();
		ResponseMessage message = new ResponseMessage();
		Error error = new Error();

		ack.setStatus(AckStatus.NACK);
		message.setAck(ack);
		response.setMessage(message);
		error.setCode(errorMessage);
		error.setMessage("Unauthorized Request.");
		response.setError(error);

		try {
			ObjectMapper mapper = new ObjectMapper();

			String header = "(created) (expires) digest";
			HttpHeaders headers = new HttpHeaders();
			headers.set("WWW-Authenticate", "Signature realm=\"" + SIGN_ALGORITHM + "\",headers=\"" + header + "\"");

			String json = mapper.writeValueAsString(response);

			return new ResponseEntity<Object>(json, headers, HttpStatus.UNAUTHORIZED);
		} catch (JsonProcessingException e) {
			log.error("Couldn't serialize response for content type application/json", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// unused
	public String buildNotAckResponse1(Context ctx, ErrorCodeOndc errorEnum) throws JsonProcessingException {

		Response response = new Response();
		ResponseMessage resMsg = new ResponseMessage();

		resMsg.setAck(new Ack(AckStatus.NACK));
		response.setMessage(resMsg);

		Error e = new Error();
		e.setType(errorEnum.getType());
		e.setCode(errorEnum.getCode());
		e.setMessage(errorEnum.getMessage());
		response.setError(e);

		ctx.setTimestamp(LocalDateTime.now().toString());
		response.setContext(ctx);
		String body = this.mapper.writeValueAsString(response);

		return body;
	}

}
