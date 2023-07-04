package com.nsdl.beckn.api.model.common;

import lombok.Data;

@Data
public class UpdatedBy {
    private Organization org;
    private Contact contact;
    private Person person;
}
