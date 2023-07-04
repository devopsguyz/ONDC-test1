package com.nsdl.beckn.common.model;

import lombok.Data;

@Data
public class BlipModel {
	private String contextDomain;
	private String contextCountry;
	private String contextCity;
	private String contextAction;
	private String contextCoreVersion;
	private String contextBapId;
	private String contextKey;
	private String contextBapUri;
	private String contextTransactionId;
	private String contextMessageId;
	private String contextTimestamp;
	private String contextTtl;
	private String subscriberType;
	private String subscriberId;
	// private String message;
	private BlipBodyModel message;
	private String loggedAt;
}
