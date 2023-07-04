package com.nsdl.beckn.api.model.onrating;

import com.nsdl.beckn.api.model.common.Context;

import lombok.Data;

@Data
public class RatingRequest {
	private Context context;
	private RatingMessage message;
}
