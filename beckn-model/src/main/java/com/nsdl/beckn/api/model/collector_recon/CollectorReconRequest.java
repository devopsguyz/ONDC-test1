package com.nsdl.beckn.api.model.collector_recon;

import com.nsdl.beckn.api.model.common.Context;

import lombok.Data;

@Data
public class CollectorReconRequest 
{
private Context context;
private CollectorReconMessage message;
}
