package com.nsdl.beckn.api.model.common;

import lombok.Data;

import java.util.List;

@Data
public class IssueActions {
    private List<ComplainantActions> complainantActions;
    private List<RespondentActions> respondentActions;
}
