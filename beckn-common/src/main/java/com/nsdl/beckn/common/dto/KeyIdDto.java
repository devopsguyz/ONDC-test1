package com.nsdl.beckn.common.dto;

import lombok.Data;

@Data
public class KeyIdDto {
	private String subscriberId;
	private String uniqueKeyId;
	private String algo;
}
