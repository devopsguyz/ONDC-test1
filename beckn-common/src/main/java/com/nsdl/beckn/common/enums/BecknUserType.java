package com.nsdl.beckn.common.enums;

public enum BecknUserType {

	BAP("BAP"),
	BPP("BPP");

	String type;
	BecknUserType(String type) {
		this.type = type;
	}

	public String type() {
		return this.type;
	}

}
