package com.nsdl.beckn.api.model.common;

import lombok.Data;

import java.util.List;

@Data
public class Issue {
    private String id;
    private String category;
    private String subCategory;
    private String issueType;
    private String status;
    private String createdAt;
    private String updatedAt;
    private String rating;

    private ComplainantInfo complainantInfo;
    private IssueOrderDetails orderDetails;
    private IssueDescriptor description;
    private IssueSource source;
    private Time expectedResponseTime;
    private Time expectedResolutionTime;
    private IssueActions issueActions;
    private FinalizedOdr finalizedOdr;
    private List<AdditionalInfoRequired> additionalInfoRequired;
    private List<SelectedOdrsInfo> selectedOdrsInfos;
    private ResolutionProvider resolutionProvider;
    private Resolution resolution;

}
