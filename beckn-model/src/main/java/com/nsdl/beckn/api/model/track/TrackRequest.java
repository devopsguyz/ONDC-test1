package com.nsdl.beckn.api.model.track;

import com.nsdl.beckn.api.model.common.Context;

import lombok.Data;

@Data
public class TrackRequest {
	private Context context;
	private TrackMessage message;
}
