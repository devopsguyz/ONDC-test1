package com.nsdl.beckn.common.builder;

import static com.nsdl.beckn.common.constant.ApplicationConstant.PIPE;
import static com.nsdl.beckn.common.constant.ApplicationConstant.REMOTE_HOST;
import static com.nsdl.beckn.common.constant.ApplicationConstant.SIGN_ALGORITHM;
import static com.nsdl.beckn.common.enums.OndcUserType.SELLER;

import java.text.MessageFormat;
import java.util.Collections;

import com.nsdl.beckn.api.model.object.ObjectRequest;
import com.nsdl.beckn.common.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.nsdl.beckn.api.model.common.Context;
import com.nsdl.beckn.common.constant.ApplicationConstant;
import com.nsdl.beckn.common.exception.ApplicationException;
import com.nsdl.beckn.common.exception.ErrorCode;
import com.nsdl.beckn.common.model.SellerOnRecordModel;
import com.nsdl.beckn.common.model.SigningModel;
import com.nsdl.beckn.common.model.SubscriberApiModel;
import com.nsdl.beckn.common.model.SubscriberModel;
import com.nsdl.beckn.common.service.SubscriberService;
import com.nsdl.beckn.common.util.SigningUtility;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HeaderBuilder {

	private static final String X_GATEWAY_AUTHORIZATION = "X-Gateway-Authorization";

	@Autowired
	private SubscriberService subscriberService;

	@Autowired
	private SigningUtility signingUtility;

	@Value("${beckn.http.header-validity: 0}")
	private int defaultHeaderValidity;

	@Value("${beckn.entity.type}")
	private String entityType;

	@Autowired
	private JsonUtil jsonUtil;

	public HttpHeaders buildHeaders(String requestBody, String providerId, SubscriberModel subscriberModel, SubscriberApiModel apiModel) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		if (apiModel.isSetAuthHeader()) {
			String authHeader = buildAuthorizationHeader(requestBody, providerId, subscriberModel, apiModel);
			headers.add(HttpHeaders.AUTHORIZATION, authHeader);
			log.info("Authorization header added to HttpHeaders");
		} else {
			log.info("Authorization header will not be set as it is switched off in the config file");
		}
		// headers.set(ALL_REQUEST_HEADERS, headers.toString());
		return headers;
	}

	public HttpHeaders buildGatewayHeaders(Context context, HttpHeaders requestHeaders, String requestBody, SubscriberModel subscriberModel,
			SubscriberApiModel apiModel) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		if (apiModel.isSetAuthHeader()) {
			String authHeader = buildAuthorizationHeader(requestBody, null, subscriberModel, apiModel);

			headers.set(HttpHeaders.AUTHORIZATION, requestHeaders.getFirst(HttpHeaders.AUTHORIZATION));
			headers.set(X_GATEWAY_AUTHORIZATION, authHeader);

			log.info("Authorization & X-Gateway-Authorization header added to HttpHeaders");
		} else {
			log.warn("Authorization & X-Gateway-Authorization header will not be set as it is switched off in the config file");
		}

		headers.set(REMOTE_HOST, "-");

		return headers;
	}

	private String buildAuthorizationHeader(String requestBody, String providerId, SubscriberModel subscriberModel, SubscriberApiModel apiModel) {
		ObjectRequest objectModel = this.jsonUtil.toModel(requestBody, ObjectRequest.class);
		boolean isError = false; // variable to identify if there is error in the response
		// if there is error in the response enable this boolean to select network participant's key
		// for signing the headers
		if (null != objectModel.getError()) {
			isError = true;
		}
		SigningModel signingModel = getSigningParams(apiModel.getName(), providerId, subscriberModel, isError);

		long currentTime = System.currentTimeMillis() / 1000L;

		Integer headerValidity = apiModel.getHeaderValidity();

		String blakeHash = this.signingUtility.generateBlakeHash(requestBody);
		log.info("BLAKE-512 hash is: {}", blakeHash);

		String signingString = "(created): " + currentTime + "\n(expires): " + (currentTime + headerValidity)
				+ "\ndigest: BLAKE-512=" + blakeHash + "";

		String signature = this.signingUtility.generateSignature(signingString, signingModel.getPrivateKey());
		String kid = signingModel.getSubscriberId() + PIPE + signingModel.getUniqueKeyId() + PIPE + SIGN_ALGORITHM;

		String authHeader = "Signature keyId=\"" + kid + "\",algorithm=\"" + SIGN_ALGORITHM + "\", created=\"" + currentTime
				+ "\", expires=\"" + (currentTime + headerValidity)
				+ "\", headers=\"(created) (expires) digest\", signature=\"" + signature + "\"";

		log.info("AuthHeader created: {}", authHeader);
		return authHeader;
	}

	private SigningModel getSigningParams(String apiName, String providerId, SubscriberModel model, boolean isError) {
		SigningModel signingModel = new SigningModel();

		String subscriberId = model.getSubscriberId();

		if (ApplicationConstant.GATEWAY_ACTIONS.contains(apiName) || !model.isMsn() || !SELLER.type().equalsIgnoreCase(this.entityType)) {
			signingModel.setSubscriberId(subscriberId);
			signingModel.setUniqueKeyId(model.getUniqueKeyId());
			signingModel.setPrivateKey(model.getPrivateKey());
			log.info("SigningModel build using entity private key {}", signingModel);

		} else {
			boolean useNpKey = false; // use network participant key when there is error and providerId is null

			if (StringUtils.isBlank(providerId)) {
				if (isError) {
					useNpKey = true;
				} else {
					String error = MessageFormat.format("subscriber[{0}] has not provided ondc_provider_id for api[{1}] in httpHeader", subscriberId, apiName);
					log.error(error);
					throw new ApplicationException(ErrorCode.ONDC_PROVIDER_ID_NOT_FOUND, error);
				}
			}

			if (useNpKey) {
				signingModel.setSubscriberId(subscriberId);
				signingModel.setUniqueKeyId(model.getUniqueKeyId());
				signingModel.setPrivateKey(model.getPrivateKey());
			} else {
				SellerOnRecordModel onRecord = this.subscriberService.getSellerOnRecord(subscriberId, providerId);
				signingModel.setSubscriberId(subscriberId);
				signingModel.setUniqueKeyId(onRecord.getUniqueKeyId());
				signingModel.setPrivateKey(onRecord.getPrivateKey());
			}
			log.info("SigningModel build using seller_on_record private key {}", signingModel);
		}

		return signingModel;
	}

}
