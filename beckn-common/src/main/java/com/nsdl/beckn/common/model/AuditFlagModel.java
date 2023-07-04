package com.nsdl.beckn.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditFlagModel {
	private boolean database;
	private boolean http;
	private boolean file;
	private boolean blip;
}
