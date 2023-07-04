package com.nsdl.beckn.api.model.cancel;

import com.nsdl.beckn.api.model.common.Descriptor;
import com.nsdl.beckn.api.model.common.Option;
import com.nsdl.beckn.api.model.common.Order;

import lombok.Data;

@Data
public class CancelMessage {
	private Order orderId;
	private Option cancellationReasonId;
	private Descriptor descriptor;
}