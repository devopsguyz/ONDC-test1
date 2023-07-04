package com.nsdl.beckn.common.enums;

public enum OndcUserTypeNew {

	BUYER_APP("buyerApp"), SELLER_APP("sellerApp"), GATEWAY("gateway");

	String type;
	OndcUserTypeNew(String type) {
		this.type = type;
	}

	public String type() {
		return this.type;
	}

}
