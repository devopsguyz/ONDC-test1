package com.nsdl.beckn.api.model.onissuestatus;

import com.nsdl.beckn.api.model.common.Issue;
import lombok.Data;

@Data
public class OnIssueRequestMessage {
    private Issue issue;
}
