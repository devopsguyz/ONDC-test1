package com.nsdl.beckn.common.service;

import static com.nsdl.beckn.api.enums.ContextAction.ON_SEARCH;
import static com.nsdl.beckn.common.constant.ApplicationConstant.REMOTE_HOST;
import static com.nsdl.beckn.common.enums.AuditType.ERROR_CALLING_BUYER;
import static com.nsdl.beckn.common.enums.AuditType.ERROR_CALLING_GATEWAY;
import static com.nsdl.beckn.common.enums.AuditType.RESPONSE_TO_BUYER;
import static com.nsdl.beckn.common.enums.AuditType.RESPONSE_TO_GATEWAY;
import static com.nsdl.beckn.common.enums.OndcUserType.SELLER;
import static com.nsdl.beckn.common.exception.ErrorCode.URL_ID_AND_SUBSCRIBER_ID_DO_NOT_MATCH;
import static java.text.MessageFormat.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.net.InetAddress;
import java.nio.file.AccessDeniedException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.nsdl.beckn.common.constant.ApplicationConstant;
import com.nsdl.beckn.common.exception.ApplicationException;
import com.nsdl.beckn.common.model.SubscriberModel;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CommonService {

	private static final String ALL = "all";

	@Autowired
	public ParameterService parameterService;

	@Autowired
	private SubscriberService subscriberService;

	@Value("${beckn.entity.type}")
	private String entityType;

	public void validateUrlIdAndSubscriberId(String urlId, String subscriberId, boolean validate) {

		if (validate) {
			SubscriberModel subscriberModel = this.subscriberService.getSubscriberByShortName(urlId);
			if (isBlank(subscriberId) || !subscriberId.equals(subscriberModel.getSubscriberId())) {
				String error = format("shortName[{0}] does not belog to subscriber[{1}]", urlId, subscriberId);
				log.error(error);
				throw new ApplicationException(URL_ID_AND_SUBSCRIBER_ID_DO_NOT_MATCH, error);
			}
		}

	}

	public String getHostId() {
		String hostname = null;
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			log.error("error while finding the hostname ", e);
		}
		return hostname;
	}

	public void validateRemoteHostIPs(SubscriberModel subscriberModel, String remoteHost) throws AccessDeniedException {

		List<String> allowedHostIpList = Arrays.asList(subscriberModel.getSubscriberIp().split(","));

		if (allowedHostIpList.stream().anyMatch(ALL::equalsIgnoreCase)) {
			log.info("no ip check will be performed as all ip's are allowed for subscriber[{}]", subscriberModel.getSubscriberId());
			return;
		}

		if (StringUtils.isBlank(remoteHost)) {
			log.error("no remote host ip found for subscriber {}", subscriberModel.getSubscriberId());
			throw new AccessDeniedException("Unauthorized to use the system. No remote host ip found");
		}

		List<String> remoteHostIpList = Arrays.asList(remoteHost.split(","));

		boolean isFound = false;

		outerloop : for (String allowedIP : allowedHostIpList) {
			for (String remoteIp : remoteHostIpList) {
				if (StringUtils.trim(remoteIp).contains(StringUtils.trim(allowedIP))) {
					isFound = true;
					log.info("{} is allowed to access the application", allowedIP);
					break outerloop;
				}
			}
		}

		if (!isFound) {
			log.error("ip[{}] is not whitelisted for subscriber {}", remoteHost, subscriberModel.getSubscriberId());
			throw new AccessDeniedException("Unauthorized to use the system. Please contant admin to get your IP whitelisted");
		}

	}

	public String getRemoteHost(HttpHeaders headers) {
		if (headers != null && CollectionUtils.isNotEmpty(headers.get(REMOTE_HOST))) {
			return headers.get(REMOTE_HOST).get(0);
		}
		return ApplicationConstant.BLANK;
	}

	public long getTimeElapsed(Instant start) {
		Instant finish = Instant.now();
		long timeElapsed = Duration.between(start, finish).toMillis();
		return timeElapsed;
	}

	public long getTimeDiff(Instant start, Instant end) {
		long timeElapsed = Duration.between(start, end).toMillis();
		return timeElapsed;
	}

	public String getErrorMessage(Throwable t) {
		String errorMsg = ExceptionUtils.getRootCauseMessage(t);

		if (StringUtils.isBlank(errorMsg)) {
			errorMsg = ExceptionUtils.getStackTrace(t);
		}
		return errorMsg;
	}

	public String getErrorCauseOrMessage(Throwable t) {
		String errorMsg = ExceptionUtils.getRootCauseMessage(t);

		if (StringUtils.isBlank(errorMsg)) {
			errorMsg = ExceptionUtils.getMessage(t);
		}
		return errorMsg;
	}

	public String getStackTrace(Throwable t) {
		return ExceptionUtils.getStackTrace(t);
	}

	public String findSuccessAuditType(String action) {
		if (SELLER.type().equalsIgnoreCase(this.entityType) && ON_SEARCH.name().equalsIgnoreCase(action)) {
			return RESPONSE_TO_GATEWAY.name();
		}
		return RESPONSE_TO_BUYER.name();
	}

	public String findErrorAuditType(String action) {
		if (SELLER.type().equalsIgnoreCase(this.entityType) && ON_SEARCH.name().equalsIgnoreCase(action)) {
			return ERROR_CALLING_GATEWAY.name();
		}
		return ERROR_CALLING_BUYER.name();
	}

	public List<String> getBlockedBuyers() {
		String paramValue = this.parameterService.getParameter(ApplicationConstant.BLOCKED_BUYERS);
		if (StringUtils.isNoneBlank(paramValue)) {
			return Arrays.asList(paramValue.split(","));
		}
		return new ArrayList<>();
	}

	public List<String> getBlockedSellers() {
		String paramValue = this.parameterService.getParameter(ApplicationConstant.BLOCKED_SELLERS);
		if (StringUtils.isNoneBlank(paramValue)) {
			return Arrays.asList(paramValue.split(","));
		}
		return new ArrayList<>();
	}

}
