package com.nsdl.beckn.api.model.onselect;

import com.nsdl.beckn.api.model.common.Order;

import lombok.Data;

@Data
public class OnSelectMessage {
	private Order order;
}
