package com.nsdl.beckn.api.model.oninit;

import com.nsdl.beckn.api.model.common.Context;

import lombok.Data;

@Data
public class OnInitRequest {
	private Context context;
	private OnInitMessage message;
}