package com.nsdl.beckn.api.model.common;

import lombok.Data;

@Data
public class RespondentInfo {
    private String type;
    private UpdatedBy organization;
    private ResolutionSupport resolutionSupport;

}
