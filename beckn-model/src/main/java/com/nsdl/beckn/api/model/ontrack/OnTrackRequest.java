package com.nsdl.beckn.api.model.ontrack;

import com.nsdl.beckn.api.model.common.Context;

import lombok.Data;

@Data
public class OnTrackRequest {
	private Context context;
	private OnTrackMessage message;
}