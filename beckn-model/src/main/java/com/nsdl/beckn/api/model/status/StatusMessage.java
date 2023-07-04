package com.nsdl.beckn.api.model.status;

import com.nsdl.beckn.api.model.common.Order;

import lombok.Data;

@Data
public class StatusMessage {
	private Order orderId;
}
