package com.nsdl.beckn.api.model.response;

import com.nsdl.beckn.api.model.common.Ack;

import lombok.Data;

@Data
public class ResponseMessage {
	private Ack ack;
}
