package com.nsdl.beckn.api.model.common;

import lombok.Data;

@Data
public class ComplainantActions {
    private String complainantAction;
    private String shortDesc;
    private String updatedAt;
    private UpdatedBy updatedBy;
}
