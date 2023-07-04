package com.nsdl.beckn.api.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class SettlementDetails {
	private String settlementCounterparty;
	private String settlementPhase;
	private String settlementType;
	private String settlementBankAccountNo;
	private String settlementIfscCode;
	private String upiAddress;
	private String bankName;
	private String branchName;
	private String beneficiaryAddress;
	private String settlementStatus;
	private String settlementReference;
	private String settlementTimestamp;
	private String settlmentAmount;

}
