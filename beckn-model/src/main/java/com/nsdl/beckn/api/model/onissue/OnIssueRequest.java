package com.nsdl.beckn.api.model.onissue;

import com.nsdl.beckn.api.model.common.Context;
import com.nsdl.beckn.api.model.issue.IssueMessage;
import lombok.Data;

@Data
public class OnIssueRequest {
    private Context context;
    private IssueMessage message;
}
