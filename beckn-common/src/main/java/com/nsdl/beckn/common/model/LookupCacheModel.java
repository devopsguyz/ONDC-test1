package com.nsdl.beckn.common.model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class LookupCacheModel implements Serializable {

	private static final long serialVersionUID = -503302491902355532L;

	private String subscriberId;
	private String uniqueKeyId;
	private List<String> city;
	private String signingPublicKey;
	private String validFrom;
	private String validUntil;
	// private String country;
	// private String encrPublicKey;
	// private String created;
	// private String updated;
}
