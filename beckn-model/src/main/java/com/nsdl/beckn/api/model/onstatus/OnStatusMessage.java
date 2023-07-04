package com.nsdl.beckn.api.model.onstatus;

import com.nsdl.beckn.api.model.common.Order;

import lombok.Data;

@Data
public class OnStatusMessage {
	private Order order;
}
