package com.nsdl.beckn.api.model.init;

import com.nsdl.beckn.api.model.common.Context;

import lombok.Data;

@Data
public class InitRequest {
	private Context context;
	private InitMessage message;
}
