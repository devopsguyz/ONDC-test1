package com.nsdl.beckn.api.model.prepareforsettle;


import com.nsdl.beckn.api.model.common.Orderbook;

import lombok.Data;

@Data
public class PrepareForSettleMessage 
{
	private Orderbook orderbook;

}
