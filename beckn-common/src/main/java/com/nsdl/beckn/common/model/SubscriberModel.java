package com.nsdl.beckn.common.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class SubscriberModel implements Serializable {

	private static final long serialVersionUID = -3911779657623471140L;

	private String subscriberId;
	private String shortName;
	private String uniqueKeyId;
	private boolean msn;
	private String privateKey;
	private String subscriberIp;
}
