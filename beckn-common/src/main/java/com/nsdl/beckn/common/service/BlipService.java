package com.nsdl.beckn.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.nsdl.beckn.common.model.BlipModel;
import com.nsdl.beckn.common.sender.Sender;
import com.nsdl.beckn.common.util.JsonUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BlipService {

	@Autowired
	private Sender sender;

	@Autowired
	private JsonUtil jsonUtil;

	@Value("${beckn.blip.url:}")
	private String loggingUrl;

	@Async(value = "httpExecutor")
	public void blip(BlipModel blipModel, String type) {

		try {
			String json = this.jsonUtil.toJson(blipModel);
			log.debug("blip json for case[{}] is {}", type, json);

			log.info("blip json for case[{}] is ready to be send", type);

			String response = this.sender.send(this.loggingUrl, new HttpHeaders(), json, null);

			log.info("response from ondc blip call is {}", response);
		} catch (Exception e) {
			log.error("exception while sending blip http call:", e);
		}
	}

}
