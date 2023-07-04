package com.nsdl.beckn.api.model.prepareforsettle;

import com.nsdl.beckn.api.model.common.Context;

import lombok.Data;


@Data
public class PrepareForSettleRequest 
{
private Context context;
private PrepareForSettleMessage message;
}
