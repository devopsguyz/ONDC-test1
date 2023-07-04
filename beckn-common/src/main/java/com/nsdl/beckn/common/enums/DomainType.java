package com.nsdl.beckn.common.enums;

public enum DomainType {

	MOBILITY("mobility");

	String type;
	DomainType(String type) {
		this.type = type;
	}

	public String type() {
		return this.type;
	}

}
