package com.nsdl.beckn.api.model.init;

import com.nsdl.beckn.api.model.common.Order;

import lombok.Data;

@Data
public class InitMessage {
	private Order order;
}
