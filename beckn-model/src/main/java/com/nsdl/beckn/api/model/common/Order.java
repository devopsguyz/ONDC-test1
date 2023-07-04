package com.nsdl.beckn.api.model.common;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Order {
	private String id;
	private String state;
	private Provider provider;
	private List<Item> items;
	private List<AddOn> addOns;
	private List<Offer> offers;
	private List<Document> documents;
	private Billing billing;
	private Fulfillment fulfillment;
	private Quotation quote;
	private Payment payment;
	private String createdAt;
	private String updatedAt;
	//For RSP
	private Correction correction;
	private String invoice_no;
	private WithHoldingTaxGst withholding_tax_gst;
	private WithHoldingTaxTds withholding_tax_tds;
	private DeductionByCollector deduction_by_collector;
	private String settlement_reason_code;
	private String payout_bank_uri;
	private String order_recon_status;
	private String reciever_app_id;
	private String receiver_app_uri;
	private String collector_app_id;
	private Payerdetails payerdetails;
	private DiffAmount diffamount;
	private String transection_id;
	private String settlement_id;
	private String settlement_reference_no;
	private String recon_status;
	private String counterparty_recon_status;
	private CounterPartyDiffAmount counterparty_diff_amount;
	private Message message;

	@JsonProperty("@ondc/org/cancellation")
	private Cancellation cancellation;

	@JsonProperty("@ondc/org/linkedOrders")
	private List<OndcLinkedOrders> linkedOrders;

	private Location providerLocation;
}
