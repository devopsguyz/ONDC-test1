package com.nsdl.beckn.common.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AuditDataModel {
	private String remoteHost;
	private String messageId;
	private String transactionId;
	private String buyerId;
	private String buyerUrl;
	private String sellerId;
	private String sellerUrl;
	private String action;
	private String domain;
	private String country;
	private String city;
	private String ttl;
	private String ctxTimestamp;
	private String ctxKey;
	private String coreVersion;
	private LocalDateTime createdOn;
	private String json;
	private String headers;
	private String type;
	private String status;
	private String error;
	private Long timeTaken;
	private String hostId;
}
