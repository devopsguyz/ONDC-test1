package com.nsdl.beckn.api.model.confirm;

import com.nsdl.beckn.api.model.common.Order;

import lombok.Data;

@Data
public class ConfirmMessage {
	private Order order;
}
