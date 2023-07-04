package com.nsdl.beckn.api.model.onselect;

import com.nsdl.beckn.api.model.common.Context;

import lombok.Data;

@Data
public class OnSelectRequest {
	private Context context;
	private OnSelectMessage message;
}