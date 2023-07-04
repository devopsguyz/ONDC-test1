package com.nsdl.beckn.api.model.common;

import lombok.Data;

import java.util.List;

@Data
public class SelectedOdrsInfo {
    private RespondentInfo respondentInfo;
    private List<FinalizedOdr> odrs;
}
