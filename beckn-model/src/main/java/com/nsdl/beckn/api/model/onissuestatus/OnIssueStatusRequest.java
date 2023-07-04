package com.nsdl.beckn.api.model.onissuestatus;

import com.nsdl.beckn.api.model.common.Context;
import lombok.Data;

@Data
public class OnIssueStatusRequest {
    private Context context;
    private OnIssueRequestMessage message;
}
