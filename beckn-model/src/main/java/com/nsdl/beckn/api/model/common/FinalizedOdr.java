package com.nsdl.beckn.api.model.common;

import lombok.Data;

@Data
public class FinalizedOdr {
    private String name;
    private String shortDesc;
    private String url;
    private UpdatedBy organization;
    private PricingModel pricingModel;
    private ResolutionRatings resolutionRatings;
}
