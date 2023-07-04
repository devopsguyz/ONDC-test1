package com.nsdl.beckn.api.model.onrecon;

import com.nsdl.beckn.api.model.common.Context;

import lombok.Data;

@Data
public class OnReconRequest {
	private Context context;
	private Object message;
}
