package com.nsdl.beckn.api.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Tags {
	
	@JsonProperty("status")
	private String status;
	
	@JsonProperty("additionalProp1")
	private String additionalProp1;

	@JsonProperty("additionalProp2")
	private String additionalProp2;

	@JsonProperty("additionalProp3")
	private String additionalProp3;
	
}
