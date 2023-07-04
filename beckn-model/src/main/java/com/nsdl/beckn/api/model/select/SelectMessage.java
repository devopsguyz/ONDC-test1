package com.nsdl.beckn.api.model.select;

import com.nsdl.beckn.api.model.common.Order;

import lombok.Data;

@Data
public class SelectMessage {
	private Order order;
}
