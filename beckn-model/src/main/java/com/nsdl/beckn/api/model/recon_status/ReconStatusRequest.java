package com.nsdl.beckn.api.model.recon_status;

import com.nsdl.beckn.api.model.common.Context;

import lombok.Data;

@Data
public class ReconStatusRequest 
{
private Context context;
private ReconStatusMessage recon_statusMessage;
}
