package com.nsdl.beckn.common.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JsonResponseModel {
	private String json;
	private String receivedOn;
	private long timeSinceReceived;

	@JsonIgnore
	private LocalDateTime createdOn;
}
