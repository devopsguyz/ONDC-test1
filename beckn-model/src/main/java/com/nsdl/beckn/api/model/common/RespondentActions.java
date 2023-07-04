package com.nsdl.beckn.api.model.common;

import lombok.Data;

@Data
public class RespondentActions {
    private String respondentAction;
    private String shortDesc;
    private String updatedAt;
    private UpdatedBy updatedBy;
    private String cascadedLevel;
}
