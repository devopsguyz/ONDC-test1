package com.nsdl.beckn.api.model.common;

import lombok.Data;

import java.util.List;

@Data
public class IssueOrderDetails {
    private String id;
    private String state;
    private List<IssueItem> items;
    private List<IssueFulfillment> fulfillments;
    private String providerId;
    private String merchantOrderId;
}
