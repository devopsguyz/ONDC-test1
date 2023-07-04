package com.nsdl.beckn.api.model.onconfirm;

import com.nsdl.beckn.api.model.common.Context;

import lombok.Data;

@Data
public class OnConfirmRequest {
	private Context context;
	private OnConfirmMessage message;
}
