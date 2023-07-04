package com.nsdl.beckn.api.model.rating;

import com.nsdl.beckn.api.model.common.Context;

import lombok.Data;

@Data
public class OnRatingRequest {
	private Context context;
	private OnRatingMessage message;
}
