package com.nsdl.beckn.common.enums;

public enum OndcUserType {

	BUYER("buyer"), SELLER("seller"), GATEWAY("gateway");

	String type;
	OndcUserType(String type) {
		this.type = type;
	}

	public String type() {
		return this.type;
	}

}
