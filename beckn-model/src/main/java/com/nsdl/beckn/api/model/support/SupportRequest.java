package com.nsdl.beckn.api.model.support;

import com.nsdl.beckn.api.model.common.Context;

import lombok.Data;

@Data
public class SupportRequest {
	private Context context;
	private SupportMessage message;
}
