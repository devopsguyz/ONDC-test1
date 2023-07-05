package com.nsdl.beckn.adaptor.controller;

import static com.nsdl.beckn.adaptor.AdaptorConstant.CONTEXT_ROOT_BUYER;
import static com.nsdl.beckn.common.constant.ApplicationConstant.REMOTE_HOST;

import java.nio.file.AccessDeniedException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.nsdl.beckn.common.model.JsonRequestModel;
import com.nsdl.beckn.common.model.JsonResponseModel;
import com.nsdl.beckn.common.model.SubscriberModel;
import com.nsdl.beckn.common.service.AuditDbService;
import com.nsdl.beckn.common.service.CommonService;
import com.nsdl.beckn.common.service.SubscriberService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class AdaptorController {

	@Autowired
	private AuditDbService service;

	@Autowired
	private SubscriberService subscriberService;

	@Autowired
	private CommonService commonService;

	@PostMapping(CONTEXT_ROOT_BUYER + "/response")
	public List<JsonResponseModel> getApiResponse(@RequestHeader HttpHeaders headers, @RequestBody JsonRequestModel model) throws AccessDeniedException {
		String remoteHost = headers.get(REMOTE_HOST).get(0);
		String subscriberId = model.getSubscriberId();

		log.info("subscriber[{}] from remote host[{}] has requested for response", subscriberId, remoteHost);

		if (StringUtils.isBlank(subscriberId)) {
			log.error("subscriberId is null. so cannot load configuration");
			throw new AccessDeniedException("Unauthorized to use the system. Please provide your subscriberId");
		}

		SubscriberModel subscriberModel = this.subscriberService.getSubscriberById(subscriberId);

		// validate remote host ip's
		this.commonService.validateRemoteHostIPs(subscriberModel, remoteHost);

		List<JsonResponseModel> list = this.service.getApiResponse(model);
		log.debug("size of response list is {}", list.size());
		return list;
	}

}
