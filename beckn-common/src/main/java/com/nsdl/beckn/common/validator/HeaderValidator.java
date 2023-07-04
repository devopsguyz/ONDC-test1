package com.nsdl.beckn.common.validator;

import static com.nsdl.beckn.common.constant.ApplicationConstant.PIPE;
import static com.nsdl.beckn.common.constant.ApplicationConstant.SIGN_ALGORITHM;
import static com.nsdl.beckn.common.exception.ErrorCode.AUTH_HEADER_NOT_FOUND;
import static com.nsdl.beckn.common.exception.ErrorCode.SIGNATURE_VERIFICATION_FAILED;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.nsdl.beckn.common.dto.KeyIdDto;
import com.nsdl.beckn.common.exception.ApplicationException;
import com.nsdl.beckn.common.exception.ErrorCode;
import com.nsdl.beckn.common.model.HeaderParams;
import com.nsdl.beckn.common.model.LookupCacheModel;
import com.nsdl.beckn.common.service.LookupService;
import com.nsdl.beckn.common.util.SigningUtility;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class HeaderValidator {

	@Autowired
	private SigningUtility signingUtility;

	@Autowired
	private LookupService lookupService;

	@Value("${beckn.entity.type}")
	private String entityType;

	@Value("${beckn.entity.id:}")
	private String entityId;

	private static final String SIGNATURE = "signature";
	private static final String KEY_ID = "keyId";

	public void validateHeader(String subscriberId, HttpHeaders httpHeaders, String requestBody, String domain, String city) {
		String authHeader = httpHeaders.getFirst(HttpHeaders.AUTHORIZATION);

		if (isBlank(authHeader)) {
			authHeader = httpHeaders.getFirst(HttpHeaders.AUTHORIZATION.toLowerCase());
		}
		if (isBlank(authHeader)) {
			throw new ApplicationException(AUTH_HEADER_NOT_FOUND);
		}

		log.info("Going to validate authHeader header {}", authHeader);

		validate(subscriberId, requestBody, authHeader, domain, true);

		// code for proxy header
		String proxyAuthHeader = getProxyHeader(httpHeaders);
		if (StringUtils.isNotBlank(proxyAuthHeader)) {
			log.info("Going to validate proxy header {}", proxyAuthHeader);
			validate(subscriberId, requestBody, proxyAuthHeader, domain, false);
		}

	}

	public String getProxyHeader(HttpHeaders httpHeaders) {
		String proxyAuthHeader = httpHeaders.getFirst(HttpHeaders.PROXY_AUTHORIZATION);
		if (isBlank(proxyAuthHeader)) {
			proxyAuthHeader = httpHeaders.getFirst(HttpHeaders.PROXY_AUTHORIZATION.toLowerCase());
		}
		return proxyAuthHeader;
	}

	private KeyIdDto validate(String subscriberId, String requestBody, String authHeader, String domain, boolean isAuthHeader) {
		Map<String, String> headersMap = this.signingUtility.parseAuthorizationHeader(authHeader);

		if (MapUtils.isEmpty(headersMap)) {
			ErrorCode errorCode = isAuthHeader ? ErrorCode.INVALID_AUTH_HEADER : ErrorCode.INVALID_PROXY_AUTH_HEADER;
			log.error(errorCode.getMessage());
			throw new ApplicationException(errorCode);
		}

		KeyIdDto keyIdDto = this.signingUtility.splitKeyId(headersMap.get(KEY_ID));
		String uniqueKeyId = keyIdDto.getUniqueKeyId();
		String lookupKey = keyIdDto.getSubscriberId() + PIPE + uniqueKeyId;
		log.info("entity key is {}", lookupKey);

		boolean allMatch = Stream.of(keyIdDto.getSubscriberId(), keyIdDto.getUniqueKeyId(), keyIdDto.getAlgo()).anyMatch(Objects::isNull);
		// boolean allMatch = Stream.of(keyIdDto.getSubscriberId(), keyIdDto.getAlgo()).anyMatch(Objects::isNull);

		String errorMsg = " for keyid " + lookupKey;
		if (allMatch) {
			ErrorCode errorCode = isAuthHeader ? ErrorCode.INVALID_KEY_ID_HEADER : ErrorCode.INVALID_PROXY_KEY_ID_HEADER;
			log.error(errorCode.getMessage() + errorMsg);
			throw new ApplicationException(errorCode);
		}

		// #Algo Validation
		String algoParam = headersMap.get("algorithm");
		if (!SIGN_ALGORITHM.equals(keyIdDto.getAlgo()) || algoParam == null || !SIGN_ALGORITHM.equals(algoParam.replace("\"", ""))) {
			ErrorCode errorCode = isAuthHeader ? ErrorCode.ALGORITHM_MISMATCH : ErrorCode.PROXY_ALGORITHM_MISMATCH;
			log.error(errorCode.getMessage() + errorMsg);
			throw new ApplicationException(errorCode);
		}

		// #Headers param validation
		HeaderParams headerParams = this.signingUtility.splitHeadersParam(headersMap.get("headers"));
		if (headerParams == null) {
			ErrorCode errorCode = isAuthHeader ? ErrorCode.INVALID_HEADERS_PARAM : ErrorCode.INVALID_PROXY_HEADERS_PARAM;
			log.error(errorCode.getMessage() + errorMsg);
			throw new ApplicationException(errorCode);
		}

		// #Timestamp Expiry check
		if (!this.signingUtility.validateTime(headersMap.get("created"), headersMap.get("expires"))) {
			ErrorCode errorCode = isAuthHeader ? ErrorCode.REQUEST_EXPIRED : ErrorCode.PROXY_REQUEST_EXPIRED;
			log.error(errorCode.getMessage() + errorMsg);
			throw new ApplicationException(errorCode);
		}
		log.info("created & expires check of auth header is successful");

		String cacheKey = lookupKey + PIPE + domain;
		LookupCacheModel lookupCacheModel = this.lookupService.getProvidersByKeyId(cacheKey);

		log.info("for cacheKey[{}], lookupResponse is {}", cacheKey, lookupCacheModel);

		// String signingPublicKey = this.helperService.findPublicKey(lookupCacheModel, uniqueKeyId);
		String signingPublicKey = lookupCacheModel.getSigningPublicKey();
		log.info("for unique_key_id[{}], signing_public_key which will be used to validate the signature is: {}", uniqueKeyId, signingPublicKey);

		if (lookupCacheModel == null || signingPublicKey == null) {
			ErrorCode errorCode = isAuthHeader ? ErrorCode.AUTH_FAILED : ErrorCode.PROXY_AUTH_FAILED;
			log.error(errorCode.getMessage() + errorMsg);
			throw new ApplicationException(errorCode);
		}

		verifySignatureUsingPublicKey(headersMap, signingPublicKey, requestBody, lookupKey);
		return keyIdDto;
	}

	private void verifySignatureUsingPublicKey(Map<String, String> headersMap, String signingPublicKey, String requestBody, String lookupKey) {
		String signed = recreateSignedString(headersMap, requestBody);
		log.info("recreated signed string: {}", signed);

		if (!this.signingUtility.verifySignature(headersMap.get(SIGNATURE).replace("\"", ""), signed, signingPublicKey, lookupKey)) {
			log.error(SIGNATURE_VERIFICATION_FAILED.toString() + " for keyid " + lookupKey);
			throw new ApplicationException(SIGNATURE_VERIFICATION_FAILED);
		}
	}

	private String recreateSignedString(Map<String, String> headersMap, String requestBody) {
		String blakeHash = this.signingUtility.generateBlakeHash(requestBody);

		StringBuilder sb = new StringBuilder();
		sb.append("(created): ");
		sb.append(headersMap.get("created").replace("\"", ""));
		sb.append("\n");
		sb.append("(expires): ");
		sb.append(headersMap.get("expires").replace("\"", ""));
		sb.append("\n");
		sb.append("digest: ");
		sb.append("BLAKE-512=" + blakeHash);

		log.info("blake hash is: {}", blakeHash);
		return sb.toString();
	}

	public boolean isAuthHeaderPresent(HttpHeaders httpHeaders) {
		String authHeader = httpHeaders.getFirst(HttpHeaders.AUTHORIZATION);

		if (isBlank(authHeader)) {
			authHeader = httpHeaders.getFirst(HttpHeaders.AUTHORIZATION.toLowerCase());
		}
		if (StringUtils.isNotBlank(authHeader)) {
			log.info("Authorization header found in the request");
			return true;
		}
		log.warn("Authorization header not found in the request");
		return false;
	}

}
