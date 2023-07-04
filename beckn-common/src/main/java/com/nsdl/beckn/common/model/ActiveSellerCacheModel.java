package com.nsdl.beckn.common.model;

import java.util.Set;

import lombok.Data;

@Data
public class ActiveSellerCacheModel {

	private String subscriberId;
	private String subscriberUrl;
	private String domain;
	private Boolean msn;
	private Set<String> networkParticipantCityCodes;

}
