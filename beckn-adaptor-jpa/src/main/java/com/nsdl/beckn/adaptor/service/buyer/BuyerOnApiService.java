package com.nsdl.beckn.adaptor.service.buyer;

import static com.nsdl.beckn.api.enums.ContextAction.ON_SEARCH;
import static com.nsdl.beckn.api.enums.ContextAction.ON_UPDATE;
import static com.nsdl.beckn.common.constant.ApplicationConstant.*;
import static com.nsdl.beckn.common.enums.AuditType.ACK_TO_GATEWAY;
import static com.nsdl.beckn.common.enums.AuditType.ACK_TO_SELLER;
import static com.nsdl.beckn.common.enums.AuditType.NACK_TO_GATEWAY;
import static com.nsdl.beckn.common.enums.AuditType.NACK_TO_SELLER;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.time.Instant;
import java.time.LocalDateTime;

import com.nsdl.beckn.common.builder.HeaderBuilder;
import com.nsdl.beckn.common.model.SubscriberModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nsdl.beckn.api.model.common.Context;
import com.nsdl.beckn.api.model.onupdate.OnUpdateRequest;
import com.nsdl.beckn.common.builder.ResponseBuilder;
import com.nsdl.beckn.common.exception.ErrorCodeOndc;
import com.nsdl.beckn.common.exception.OndcException;
import com.nsdl.beckn.common.model.AuditFlagModel;
import com.nsdl.beckn.common.model.AuditModel;
import com.nsdl.beckn.common.model.SubscriberApiModel;
import com.nsdl.beckn.common.service.AuditService;
import com.nsdl.beckn.common.service.CommonService;
import com.nsdl.beckn.common.service.SubscriberService;
import com.nsdl.beckn.common.util.JsonUtil;
import com.nsdl.beckn.common.validator.HeaderValidator;
import com.nsdl.beckn.api.model.common.Order;
import com.nsdl.beckn.api.model.common.QuotationBreakUp;
import com.nsdl.beckn.api.model.common.Tags;
import com.nsdl.beckn.api.model.common.Item;

import java.util.Map;
import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BuyerOnApiService {

	@Autowired
	private SubscriberService subscriberService;

	@Autowired
	private AuditService auditService;

	@Autowired
	private HeaderValidator validator;

	@Autowired
	private ResponseBuilder responseBuilder;

	@Autowired
	private CommonService commonService;

	@Autowired
	private HeaderBuilder authHeaderBuilder;

	@Autowired
	private SellerCaller sellerCaller;
	
	@Autowired
	private JsonUtil jsonUtil;

	public ResponseEntity<String> callback(String api, HttpHeaders headers, String body, Context ctx, Instant start) throws Exception {

		String source = "seller";

		boolean isGateway = false;
		if (ON_SEARCH.value().equalsIgnoreCase(ctx.getAction())) {
			log.warn("reply has come from gateway on_search");
			isGateway = true;
			source = "gateway";
		}

		try {

			String bppId = ctx.getBppId();
			log.warn("{} is send by: {}", api, bppId);
			String configId = ctx.getBapId();
			if (ON_RECEIVER_RECON.equalsIgnoreCase(ctx.getAction())) {
				configId = ctx.getBppId();
			}
			SubscriberModel configModel = this.subscriberService.getSubscriberById(configId);
			SubscriberApiModel apiModel = this.subscriberService.getSubscriberApi(configId, api);

			// header validation starts
			boolean authenticate = apiModel.isValidateAuthHeader();
			log.info("does {} {} requires to be authenticated ? {}", source, bppId, authenticate);

			if (authenticate) {
				try {
					this.validator.validateHeader(bppId, headers, body, ctx.getDomain(), ctx.getCity());
				} catch (Exception ex) {
					log.error("{} header validation failed", source, ex);
					throw new OndcException(ErrorCodeOndc.BUYER_20001);
				}
			}
			

			if(ON_UPDATE.value().equalsIgnoreCase(ctx.getAction())) {				
				validateOnUpdateJSONBody(body, ctx);
			}

			ResponseEntity<String> response = this.responseBuilder.buildResponseEntity(ctx);
			log.info("Response to seller " +response);

			// do required http call and audits
			audit2(api, headers, body, ctx, isGateway, response.toString(), start);
			if (ON_RECEIVER_RECON.equalsIgnoreCase(ctx.getAction()) ||
					ON_COLLECTOR_RECON.equalsIgnoreCase(ctx.getAction()) ||
					ON_RECON_STATUS.equalsIgnoreCase(ctx.getAction())) {
				// set auth header
				HttpHeaders headers1 = this.authHeaderBuilder.buildHeaders(body, null, configModel, apiModel);
				log.info("auth header build completed");
				this.sellerCaller.callSeller(api, headers1, body, ctx, apiModel);
			}

			log.info("returning the ack to {} {} for txId {} & msgId {}", source, ctx.getBppId(), ctx.getTransactionId(), ctx.getMessageId());

			return response;

		} catch (Exception e) {
			log.error("error in buyer on_api service", e);

			HttpStatus httpStatus = null;
			ErrorCodeOndc errorCode = null;
			String errorMsg = this.commonService.getErrorMessage(e);

			if (e instanceof OndcException) {
				OndcException gex = (OndcException) e;
				errorCode = gex.getErrorCode();
				httpStatus = ErrorCodeOndc.BUYER_20001 == gex.getErrorCode() ? UNAUTHORIZED : OK;
			} else {
				httpStatus = OK;
				errorCode = ErrorCodeOndc.BUYER_20002;
			}

			String response = this.responseBuilder.buildNotAckResponse(ctx, errorCode, errorMsg);

			// do audit
			AuditModel errorFields = AuditModel.builder()
					.type(isGateway ? NACK_TO_GATEWAY.name() : NACK_TO_SELLER.name())
					.headers(headers).body(body).context(ctx)
					.startTime(start)
					.endTime(Instant.now())
					.createdOn(LocalDateTime.now())
					.blipMsg(response)
					.auditFlags(AuditFlagModel.builder().blip(true).database(true).build())
					.build();

			this.auditService.audit(errorFields);
			// end of audit

			return new ResponseEntity<>(response, httpStatus);
		}
	}

	private void audit2(String api, HttpHeaders headers, String body, Context ctx, boolean isGateway, String response, Instant start) {
		String type = isGateway ? ACK_TO_GATEWAY.name() : ACK_TO_SELLER.name();

		AuditModel fields = AuditModel.builder()
				.type(type)
				.headers(headers).body(body).context(ctx)
				.startTime(start)
				.endTime(Instant.now())
				.createdOn(LocalDateTime.now())
				.blipMsg(response)
				.auditFlags(AuditFlagModel.builder().http(true).database(true).build())
				.build();

		this.auditService.audit(fields);
	}
	
	//Change Request from Kotak
	public void validateOnUpdateJSONBody(String body, Context ctx) throws JsonProcessingException {
		
		Map<String,Integer> itemIdPresentInItemArray = new HashMap<>();
		
		Order order = new Order();
		
		try {
			log.info("Validating the JSON body");
			OnUpdateRequest objectModel = this.jsonUtil.toModel(body, OnUpdateRequest.class);
			order = objectModel.getMessage().getOrder();
			
			if(order != null) {
				Integer itemArrayListSize = order.getItems().size();
				if(itemArrayListSize != 0) {
					for(int i = 0; i < itemArrayListSize; i++) {
						
						Item items = order.getItems().get(i);
						String id = items.getId();
						Tags tags = items.getTags();
						Integer quantityCount = null;
						
						if(items.getQuantity() != null) {
							quantityCount = items.getQuantity().getCount();
						}
							
						if(tags != null) {
							if (tags.getStatus() != null && 
									quantityCount != null &&
									"Liquidated".trim().equalsIgnoreCase(tags.getStatus())) {
							    quantityCount = 0;
							}
						}
						
						//taking the IDs from the Item array and storing quantity count for corresponding ID
						if(itemIdPresentInItemArray.containsKey(id)) {
							Integer existingCount = itemIdPresentInItemArray.get(id);
							itemIdPresentInItemArray.put(id, existingCount + quantityCount);
						} else {
							itemIdPresentInItemArray.put(id, quantityCount);
						}
					
					}
				}
				
			}
			
			
			
			log.info("Map after adding the ID and Quantity: " +itemIdPresentInItemArray);
			
			//validation for non-liquidated and liquidated items
			for (Map.Entry<String, Integer> entry : itemIdPresentInItemArray.entrySet()) {
				String itemId = entry.getKey();
	            boolean idPresent = false;
	            
	            Integer quoteBreakupArrayListSize = order.getQuote().getBreakup().size();
	            
	            if(quoteBreakupArrayListSize != 0) {
	            	for (int i = 0; i < quoteBreakupArrayListSize; i++) {
		            	
	            		QuotationBreakUp quote = order.getQuote().getBreakup().get(i);
		            	String quoteItemId = quote.getItemId();
		            	
		            	if (itemId.equals(quoteItemId)) {
		            		idPresent = true;
		            		
		            		// Check if the quantity matches
		                    Integer quantityCountInItem = entry.getValue();
		                    Integer quantityCountInBreakup = quote.getItemQuantity().getCount();
		                    
		                    if (quantityCountInItem != null && 
		                    		quantityCountInBreakup != null && 
		                    		!quantityCountInItem.equals(quantityCountInBreakup)) {
		                    	
		                    	log.info("Count quatity for item " +itemId+ " does not match in Items and Breakup array");
		                    	throw new RuntimeException();
		                    }
		                    break;
		            	}
		            }
	            }
	            
	            if(!idPresent) {
	            	log.info("Item ID " +itemId+ " is not present in the Quote Breakup array");
	            	throw new RuntimeException();
	            }
			}
	            
			
		} catch (Exception e) {
			throw new OndcException(ErrorCodeOndc.BUYER_22503);
        } 
		
	}	

}

