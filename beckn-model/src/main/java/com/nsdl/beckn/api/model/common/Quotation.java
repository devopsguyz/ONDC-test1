package com.nsdl.beckn.api.model.common;

import java.util.List;

import lombok.Data;

@Data
public class Quotation {
	private Price price;
	private List<QuotationBreakUp> breakup;
	private String ttl;
}
