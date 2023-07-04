package com.nsdl.beckn.api.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class QuotationBreakUp {

	@JsonProperty("@ondc/org/item_id")
	private String itemId;

	@JsonProperty("@ondc/org/item_quantity")
	private ItemQuantity itemQuantity;

	@JsonProperty("@ondc/org/title_type")
	private String titleType;

	private String title;
	private Price price;
}
