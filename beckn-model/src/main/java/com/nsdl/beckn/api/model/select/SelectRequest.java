package com.nsdl.beckn.api.model.select;

import com.nsdl.beckn.api.model.common.Context;

import lombok.Data;

@Data
public class SelectRequest {
	private Context context;
	private SelectMessage message;
}