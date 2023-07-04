package com.nsdl.beckn.common.model;

import java.util.List;

import lombok.Data;

@Data
public class JsonRequestModel {
	private String subscriberId;
	private String messageId;
	private String transactionId;
	private String action;
	private String status;
	private String recordRange;
	private List<String> sellers;
}
