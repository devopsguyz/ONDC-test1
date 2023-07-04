package com.nsdl.beckn.common.exception;

public enum ErrorCodeOndc {
	GATEWAY_10000("Gateway", "10000", "Bad or Invalid request error", "Generic bad or invalid request error"),
	GATEWAY_10001("Gateway", "10001", "Invalid Signature", "Cannot verify signature for request"),
	GATEWAY_10002("Gateway", "10002", "Invalid City Code", "Valid city code needs to be passed for search"),

	BUYER_20000("BuyerApp", "20000", "Invalid catalog item", "Catalog Item cannot be displayed as it does not meet statutory requirements"),
	BUYER_20001("BuyerApp", "20001", "Invalid Signature", "Cannot verify signature for request"),
	BUYER_20002("BuyerApp", "20002", "Business Error", "Generic business error"),
	BUYER_22503("DOMAIN-ERROR", "22503", "Updated quote does not match original order value and cancellation terms", "Updated quote does not match original order value and cancellation terms"),

	SELLER_30016("SellerApp", "30016", "Invalid Signature", "Cannot verify signature for request"),
	SELLER_40000("SellerApp", "40000", "Business Error", "Generic business error"),
	
	;

	private String type;
	private String code;
	private String message;
	private String description;

	ErrorCodeOndc(final String type, final String code, final String message, final String description) {
		this.type = type;
		this.code = code;
		this.message = message;
		this.description = description;
	}

	public String getType() { return this.type; }
	public String getCode() { return this.code; }
	public String getMessage() { return this.message; }
	public String getDescription() { return this.description; }

}