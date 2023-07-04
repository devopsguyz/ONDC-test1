package com.nsdl.beckn.api.model.common;

import lombok.Data;

@Data
public class InfoRequired {
    private IssueDescriptor description;
    private String updatedAt;
    private String messageId;
}
