package com.nsdl.beckn.common.service;

import static com.nsdl.beckn.common.exception.ErrorCode.NO_SUBSCRIBER_CONFIGURED;

import java.text.MessageFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nsdl.beckn.common.entity.ParameterEntity;
import com.nsdl.beckn.common.exception.ApplicationException;

@Service
public class ParameterService {

	@Autowired
	private CacheService service;

	public String getParameter(String paramName) {
		List<ParameterEntity> list = this.service.getParameterList();
		ParameterEntity entity = list
				.stream()
				.filter(e -> e.getKey().equalsIgnoreCase(paramName))
				.findFirst()
				.orElseThrow(() -> {
					String error = MessageFormat.format("parameter [{0}] not configured in the database", paramName);
					throw new ApplicationException(NO_SUBSCRIBER_CONFIGURED, error);
				});

		return entity.getValue();

	}

}
