package com.nsdl.beckn.api.model.onupdate;

import com.nsdl.beckn.api.model.common.Context;

import lombok.Data;

@Data
public class OnUpdateRequest {
	private Context context;
	private OnUpdateMessage message;
}