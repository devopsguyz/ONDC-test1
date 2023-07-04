package com.nsdl.beckn.api.model.onconfirm;

import com.nsdl.beckn.api.model.common.Order;

import lombok.Data;

@Data
public class OnConfirmMessage {
	private Order order;
}
