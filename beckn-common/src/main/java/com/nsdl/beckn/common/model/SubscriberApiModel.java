package com.nsdl.beckn.common.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class SubscriberApiModel implements Serializable {

	private static final long serialVersionUID = 270298869766497237L;

	private String subscriberId;
	private String name;
	private String endpoint;
	private short timeout;
	private short retryCount;
	private int headerValidity;
	private boolean validateAuthHeader;
	private boolean setAuthHeader;
}
