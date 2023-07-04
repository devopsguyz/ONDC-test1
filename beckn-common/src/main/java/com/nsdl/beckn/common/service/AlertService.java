package com.nsdl.beckn.common.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.nsdl.beckn.common.entity.AlertConfigEntity;
import com.nsdl.beckn.common.model.AlertModel;
import com.nsdl.beckn.common.model.AuditModel;
import com.nsdl.beckn.common.sender.Sender;
import com.nsdl.beckn.common.util.JsonUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AlertService {

	@Autowired
	private Sender sender;

	@Autowired
	private JsonUtil jsonUtil;

	@Value("${beckn.alert.url}")
	private String url;

	@Async(value = "httpExecutor")
	public void sendAlert(AuditModel auditModel, AlertConfigEntity configEntity) {
		log.info("going to raise alert...");
		AlertModel alertModel = new AlertModel();
		alertModel.setRequestId(auditModel.getContext().getTransactionId());
		alertModel.setRequest(auditModel.getBody());
		alertModel.setPriority(configEntity.getPriority());
		alertModel.setException(auditModel.getErrorStackTrace());
		alertModel.setCreatedOn(LocalDateTime.now());

		String json = this.jsonUtil.toJson(alertModel);

		String reposne = this.sender.send(this.url, new HttpHeaders(), json, null);
		log.info("response from sending alert {}", reposne);

	}
}
