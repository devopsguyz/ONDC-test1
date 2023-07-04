package com.nsdl.beckn.common.model;

import lombok.Data;

@Data
public class AuthDetailsModel {
	private Integer status;
	private String msg;
	private Integer regId;
	private String publicCert;
	private String keyId;

}
