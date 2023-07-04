package com.nsdl.beckn.api.model.onupdate;

import com.nsdl.beckn.api.model.common.Order;

import lombok.Data;

@Data
public class OnUpdateMessage {
	private Order order;
}
