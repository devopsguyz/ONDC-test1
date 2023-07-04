package com.nsdl.beckn.api.model.issue;

import com.nsdl.beckn.api.model.common.Context;
import lombok.Data;

@Data
public class IssueRequest {
    private Context context;
    private IssueMessage message;
}
