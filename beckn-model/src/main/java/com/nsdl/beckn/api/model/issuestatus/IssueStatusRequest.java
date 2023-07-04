package com.nsdl.beckn.api.model.issuestatus;

import com.nsdl.beckn.api.model.common.Context;
import lombok.Data;

@Data
public class IssueStatusRequest {
    private Context context;
    private IssueStatusMessage message;
}
