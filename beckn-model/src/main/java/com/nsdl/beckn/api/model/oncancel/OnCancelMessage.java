package com.nsdl.beckn.api.model.oncancel;

import com.nsdl.beckn.api.model.common.Order;

import lombok.Data;

@Data
public class OnCancelMessage {
	private Order order;
}
