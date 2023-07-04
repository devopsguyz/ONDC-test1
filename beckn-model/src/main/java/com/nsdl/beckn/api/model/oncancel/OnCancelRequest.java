package com.nsdl.beckn.api.model.oncancel;

import com.nsdl.beckn.api.model.common.Context;

import lombok.Data;

@Data
public class OnCancelRequest {
	private Context context;
	private OnCancelMessage message;
}