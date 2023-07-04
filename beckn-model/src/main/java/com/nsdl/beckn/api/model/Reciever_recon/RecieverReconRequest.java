package com.nsdl.beckn.api.model.Reciever_recon;

import com.nsdl.beckn.api.model.common.Context;

import lombok.Data;

@Data
public class RecieverReconRequest 
{
private Context context;
private RecieverReconMessage recieverReconMessage; 
}
