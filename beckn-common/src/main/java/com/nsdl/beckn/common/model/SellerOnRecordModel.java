package com.nsdl.beckn.common.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class SellerOnRecordModel implements Serializable {

	private static final long serialVersionUID = 8786049342943663277L;

	private String subscriberId;
	private String uniqueKeyId;
	private String providerId;
	private String privateKey;
}
