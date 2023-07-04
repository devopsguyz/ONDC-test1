package com.nsdl.beckn.api.model.common;

import lombok.Data;

import java.util.List;

@Data
public class ResolutionSupport {
    private String chatLink;
    private Contact contact;
    private List<RespondentFaqs> respondentFaqs;
    private List<AdditionalSources> additionalSources;
    private List<Gros> gros;

}
