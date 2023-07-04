package com.nsdl.beckn.common.exception;

import static com.nsdl.beckn.common.constant.ApplicationConstant.PIPE;
import static com.nsdl.beckn.common.exception.ErrorCode.AUTH_FAILED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.nio.file.AccessDeniedException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	private static final String BODY_ERRO_RESPONSE = "Something went wrong";

	@ExceptionHandler(value = {Throwable.class, RuntimeException.class})
	protected ResponseEntity<Object> handleConflict(Exception ex, WebRequest request) {
		log.info("The excpetion caught is:", ex);

		HttpHeaders headers = new HttpHeaders();

		if (ex instanceof ApplicationException) {
			log.error("The error is of type ApplicationException");
			ApplicationException appEx = (ApplicationException) ex;
			if (AUTH_FAILED.getCode() == appEx.getErrorCode().getCode()) {
				log.error("Auth failed");
				return handleExceptionInternal(ex, AUTH_FAILED.getMessage(), headers, UNAUTHORIZED, request);
			}

			String errorMessage = buildErrorMessage(ex);
			log.info("error message to return is {}", errorMessage);
			return handleExceptionInternal(ex, errorMessage, headers, HttpStatus.BAD_REQUEST, request);
		}
		if (ex instanceof WebClientRequestException) {
			log.error("The error is of type WebClientRequestException");
			WebClientRequestException requestExp = (WebClientRequestException) ex;
			return handleExceptionInternal(ex, requestExp.getMessage(), headers, INTERNAL_SERVER_ERROR, request);
		}
		if (ex instanceof WebClientResponseException) {
			log.error("The error is of type WebClientResponseException");
			WebClientResponseException responseExp = (WebClientResponseException) ex;
			return handleExceptionInternal(ex, responseExp.getMessage(), headers, responseExp.getStatusCode(), request);
		}
		if (ex instanceof TimeoutException) {
			log.error("The error is of type TimeoutException");
			TimeoutException timeoutExp = (TimeoutException) ex;
			return handleExceptionInternal(ex, timeoutExp.getMessage(), headers, HttpStatus.REQUEST_TIMEOUT, request);
		}
		if (ex instanceof AccessDeniedException) {
			log.error("The error is of type AccessDeniedException");
			AccessDeniedException accessDeniedExp = (AccessDeniedException) ex;
			return handleExceptionInternal(ex, accessDeniedExp.getMessage(), headers, HttpStatus.UNAUTHORIZED, request);
		}

		log.error("Generic error will be thrown");
		return handleExceptionInternal(ex, BODY_ERRO_RESPONSE, headers, INTERNAL_SERVER_ERROR, request);
	}

	private String buildErrorMessage(Exception ex) {
		if (NumberUtils.isDigits(ex.getMessage())) {
			ErrorCode errorCode = ((ApplicationException) ex).getErrorCode();
			return errorCode.getCode() + PIPE + errorCode.getMessage();
		}
		return ex.getMessage();
	}
}