package com.nsdl.beckn.api.model.lookup;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class LookupResponse implements Serializable {
	private static final long serialVersionUID = -346294589080605953L;

	private String subscriberId;
	private String uniqueKeyId;
	private String country;
	private List<String> city;
	private String signingPublicKey;
	private String encrPublicKey;
	private String validFrom;
	private String validUntil;
	private String created;
	private String updated;

	private List<NetworkParticipant> networkParticipant;
}
