package com.nsdl.beckn.api.model.common;

import lombok.Data;

import java.util.List;

@Data
public class IssueDescriptor {
    private String shortDesc;
    private String longDesc;
    private AdditionalDescription additionalDesc;
    private List<String> images;
}
