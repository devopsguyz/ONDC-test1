package com.nsdl.beckn.api.model.on_reconStatus;

import com.nsdl.beckn.api.model.common.Context;

import lombok.Data;

@Data
public class OnReconStatusRequest 
{
private Context context;
private OnReconStatusMessage onRecon_statusMessage;
}
