package com.nsdl.beckn.common.service;

import static com.nsdl.beckn.common.constant.ApplicationConstant.PIPE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nsdl.beckn.api.model.common.Context;
import com.nsdl.beckn.common.constant.ApplicationConstant;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GatewayCommonService {

	@Autowired
	private LookupServiceGateway lookupServiceGateway;

	@Value("${beckn.entity.id: }")
	private String entityId;

	public String findGatewayKeyId(Context context) {
		// LookupRequest lookupRequest = new LookupRequest();
		// LookupResponse lookupResponse = this.lookupServiceGateway.getGatewayProviders(context, lookupRequest);
		// String uniqueKeyId = lookupResponse.getUniqueKeyId();
		String uniqueKeyId = ApplicationConstant.BLANK;
		log.info("UniqueKeyId for this gateway[{}] is: {}", this.entityId, uniqueKeyId);
		return this.entityId + PIPE + uniqueKeyId;
	}
}
