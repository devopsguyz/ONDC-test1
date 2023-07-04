package com.nsdl.beckn.common.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AlertModel {
	private String requestId;
	private String request;
	private String serverName;
	private String priority;
	private String exception;
	private String javaStackTrace;
	private LocalDateTime createdOn;
}
