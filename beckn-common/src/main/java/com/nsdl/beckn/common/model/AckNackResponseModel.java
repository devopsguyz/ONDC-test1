package com.nsdl.beckn.common.model;

import lombok.Data;

@Data
public class AckNackResponseModel {
	private boolean isAck;
	private String responseEnum;
	private String response;

}
