package com.nsdl.beckn.common.builder;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.nsdl.beckn.api.model.common.Context;
import com.nsdl.beckn.common.model.AuditModel;
import com.nsdl.beckn.common.model.BlipBodyModel;
import com.nsdl.beckn.common.model.BlipModel;

@Component
public class ModelBuilder {

	@Value("${beckn.entity.id:}")
	private String entityId;

	public BlipModel buildBlipModel(AuditModel auditModel) {
		Context context = auditModel.getContext();

		BlipModel model = new BlipModel();
		model.setContextDomain(context.getDomain());
		model.setContextCountry(context.getCountry());
		model.setContextCity(context.getCity());
		model.setContextAction(context.getAction());
		model.setContextCoreVersion(context.getCoreVersion());
		model.setContextBapId(context.getBapId());
		model.setContextKey(context.getKey());
		model.setContextBapUri(context.getBapUri());
		model.setContextTransactionId(context.getTransactionId());
		model.setContextMessageId(context.getMessageId());
		model.setContextTimestamp(context.getTimestamp());
		model.setContextTtl(context.getTtl());
		model.setSubscriberType("bg");
		model.setSubscriberId(this.entityId);
		model.setLoggedAt(LocalDateTime.now().toString());

		BlipBodyModel responseModel = new BlipBodyModel();
		responseModel.setJson(auditModel.getBlipMsg());

		model.setMessage(responseModel);

		return model;
	}

}
