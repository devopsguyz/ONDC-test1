package com.nsdl.beckn.api.model.status;

import com.nsdl.beckn.api.model.common.Context;

import lombok.Data;

@Data
public class StatusRequest {
	private Context context;
	private StatusMessage message;
}
