package com.nsdl.beckn.api.model.oninit;

import com.nsdl.beckn.api.model.common.Order;

import lombok.Data;

@Data
public class OnInitMessage {
	private Order order;
}
