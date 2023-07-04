package com.nsdl.beckn.api.model.common;

import lombok.Data;

@Data
public class Resolution {
    private String shortDesc;
    private String longDesc;
    private String groRemarks;
    private String odrRemarks;
    private String actionTriggered;
    private String resolutionAction;

}
