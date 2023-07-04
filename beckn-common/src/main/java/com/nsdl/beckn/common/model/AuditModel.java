package com.nsdl.beckn.common.model;

import java.time.Instant;
import java.time.LocalDateTime;

import org.springframework.http.HttpHeaders;

import com.nsdl.beckn.api.model.common.Context;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuditModel {
	private String type;
	private HttpHeaders headers;
	private String body;
	private Context context;
	private String errorStackTrace;
	private String errorTechnical;
	private String errorFunctional;
	private Instant startTime;
	private Instant endTime;
	private LocalDateTime createdOn;
	private String blipMsg;

	private AuditFlagModel auditFlags;
}
