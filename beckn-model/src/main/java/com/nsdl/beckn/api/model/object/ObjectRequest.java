package com.nsdl.beckn.api.model.object;

import com.nsdl.beckn.api.model.common.Context;
import com.nsdl.beckn.api.model.common.Error;

import lombok.Data;

@Data
public class ObjectRequest {
	private Context context;
	private Object message;
	private Error error;
}
