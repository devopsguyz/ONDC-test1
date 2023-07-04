package com.nsdl.beckn.api.model.onsearch;

import com.nsdl.beckn.api.model.common.Context;

import lombok.Data;

@Data
public class OnSearchRequest {
	private Context context;
	private OnSearchMessage message;
}
