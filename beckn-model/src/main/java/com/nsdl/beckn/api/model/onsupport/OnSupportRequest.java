package com.nsdl.beckn.api.model.onsupport;

import com.nsdl.beckn.api.model.common.Context;

import lombok.Data;

@Data
public class OnSupportRequest {
	private Context context;
	private OnSupportMessage message;
}
