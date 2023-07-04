package com.nsdl.beckn.common.exception;

public enum ErrorCode {
	SUBSCRIBER_NOT_FOUND(1000, "Subscriber not found"),
	NO_SUBSCRIBER_CONFIGURED(1001, "No subscriber found configured"),
	AUTH_FAILED(1005, "Authentication failed"),
	INVALID_KEY_ID_HEADER(1010, "Invalid keys are found in the header"),
	INVALID_AUTH_HEADER(1015, "The auth header is not valid"),
	AUTH_HEADER_NOT_FOUND(1020, "The auth header not found"),
	BAD_REQUEST(1025, "Bad request"),
	ALGORITHM_MISMATCH(1030, "There is mismatch in the algorithm"),
	REQUEST_EXPIRED(1035, "The request has expired"),
	INVALID_HEADERS_PARAM(1040, "Invalid headers are present in header parameters"),
	SIGNATURE_VERIFICATION_FAILED(1045, "The signature verification failed"),
	REQ_DIGEST_MISMATCH(1050, "Request digest mismatch"),
	SCHEMA_VALIDATION_FAILED(1056, "Invalid request. Schema validation failed"),
	INVALID_REQUEST(1055, "Invalid request"),
	REQUEST_ALREADY_IN_PROCESS(1060, "The request is already in process"),
	INVALID_DOMAIN(1065, "Invalid domain"),
	INVALID_CITY(1066, "Invalid city"),
	NO_GATEWAY_FOUND(1067, "No gateway found for the requested domain"),
	UNKNOWN_ERROR(1070, "The Service faced a fatal technical exception"),
	REQUEST_ALREADY_PROCESSED(1075, "The request is already processed"),
	TRANSACTION_ID_NOT_PRESENT(1080, "Transaction id not found in the request"),
	INVALID_ACTION(1085, "Invalid action"),
	INVALID_CONTENT_TYPE(1090, "Invalid content type. Only application/json allowed"),
	HEADER_SEQ_MISMATCH(1095, "Header sequence mismatched"),
	HEADER_PARSING_FAILED(1100, "Header parsing failed"),
	INVALID_BPP(1105, "BPP is not valid/not registered in NSDL system"),
	JSON_PROCESSING_ERROR(1110, "Issue while preparing the json"),
	INVALID_ENTITY_TYPE(1115, "Invalid entity type configured in config file"),
	HTTP_TIMEOUT_ERROR(1120, "Http timeout error"),
	NETWORK_ERROR(1125, "Issue while making http call"),
	ADAPTOR_ERROR_CALLING_GATEWAY(1126, "Issue while calling gateway. Please contanct admin"),
	ADAPTOR_ERROR_CALLING_BUYER(1127, "Issue while calling buyer. Please contanct admin"),
	ADAPTOR_ERROR_CALLING_SELLER(1128, "Issue while calling seller. Please contanct admin"),
	CERTIFICATE_ALIAS_ERROR(1130, "Required alias not found in the certificate"),
	CERTIFICATE_ERROR(1135, "Error while loading the certificate"),
	SIGNATURE_ERROR(1140, "Error while generating the signature"),
	BLOCKED_BUYER_ERROR(1160, "The buyer is blocked to received any callback"),
	BLOCKED_SELLER_ERROR(1170, "The seller is blocked to send any request"),
	API_NOT_FOUND(1200, "Api not configured in database"),
	SELLER_ON_RECORD_NOT_FOUND(1200, "The seller_on_record not found"),
	ONDC_PROVIDER_ID_NOT_FOUND(1210, "The ondc_provider_id not found in HttpHeader"),
	URL_ID_AND_SUBSCRIBER_ID_DO_NOT_MATCH(1220, "UrlId does not belong to given subscriber"),

	// proxy header error
	PROXY_HEADER_NOT_FOUND(2000, "The proxy header not found"),
	PROXY_AUTH_FAILED(2001, "Proxy Authentication failed"),
	INVALID_PROXY_KEY_ID_HEADER(2002, "Invalid proxy keyid is found in the header"),
	INVALID_PROXY_AUTH_HEADER(2003, "The proxy auth header is not valid"),
	PROXY_ALGORITHM_MISMATCH(2005, "There is mismatch in the algorithm of proxy header"),
	PROXY_REQUEST_EXPIRED(2006, "The proxy request has expired"),
	INVALID_PROXY_HEADERS_PARAM(2007, "Invalid headers are present in proxy header parameters"),
	;

	private int code;
	private String message;

	ErrorCode(final int errorCode, final String errorMessage) {
		this.code = errorCode;
		this.message = errorMessage;
	}

	public int getCode() { return this.code; }

	public void setCode(final int errorCode) { this.code = errorCode; }

	public String getMessage() { return this.message; }

	public void setMessage(final String errorMessage) { this.message = errorMessage; }
}