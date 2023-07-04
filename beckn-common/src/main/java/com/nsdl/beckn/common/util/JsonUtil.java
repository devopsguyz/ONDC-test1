package com.nsdl.beckn.common.util;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nsdl.beckn.common.exception.ApplicationException;
import com.nsdl.beckn.common.exception.ErrorCode;
import com.nsdl.beckn.common.service.AuditDbService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@Primary
public class JsonUtil {

	@Autowired
	@Qualifier("snakeCaseObjectMapper")
	private ObjectMapper mapper;

	@Autowired
	private AuditDbService auditDbService;

	@Autowired
	private AdaptorUtil adaptorUtil;

	@Value("${beckn.persistence.audit-schema-error}")
	private boolean auditSchemaError;

	public String toJson(Object request) {
		try {
			return this.mapper.writeValueAsString(request);
		} catch (Exception e) {
			log.error("error while building json", e);
			throw new ApplicationException(ErrorCode.JSON_PROCESSING_ERROR);
		}
	}

	public String toJsonOrDefault(Object request) {

		try {
			if (ObjectUtils.isNotEmpty(request)) {
				return this.mapper.writeValueAsString(request);
			}
			return String.valueOf(request);
		} catch (Exception e) {
			log.error("error while building json in toJsonOrDefault", e);
		}
		return String.valueOf(request);
	}

	@SuppressWarnings("unchecked")
	public <schemaClass> schemaClass toModel(String body, Class<?> schemaClass) {
		try {
			schemaClass model = (schemaClass) this.mapper.readValue(body, schemaClass);
			log.debug("The {} model is {}", schemaClass, model);
			return model;
		} catch (JsonProcessingException e) {
			log.error("Not a valid json. Error while json processing for {}", schemaClass, e);

			boolean isDbConfigured = this.adaptorUtil.isDataBasePersistanceConfigured();
			if (isDbConfigured && this.auditSchemaError) {
				log.info("Going to audit error due to schema validation failure");
				auditSchemaError(schemaClass.getSuperclass(), body, e.toString());
			}
			throw new ApplicationException(ErrorCode.INVALID_REQUEST);
		}
	}

	@SuppressWarnings("unchecked")
	public <schemaClass> schemaClass validateSchema(String body, Class<?> schemaClass) {
		try {
			schemaClass model = (schemaClass) this.mapper.readValue(body, schemaClass);
			log.debug("The {} model in validate schema is {}", schemaClass, model);
			return model;
		} catch (Exception e) {
			log.error("Error while json processing for {}. Not a valid json {}", schemaClass, e);
			throw new ApplicationException(ErrorCode.SCHEMA_VALIDATION_FAILED, e.getMessage());
		}

	}

	@SuppressWarnings("unchecked")
	public <schemaClass> schemaClass toModelList(String body, Class<?> schemaClass) {
		try {
			schemaClass model = (schemaClass) this.mapper.readValue(body, this.mapper.getTypeFactory().constructCollectionType(List.class, schemaClass));
			log.debug("The {} model list is {}", schemaClass, model);
			return model;
		} catch (JsonProcessingException e) {
			log.error("Error while json processing for {}. Not a valid json {}", schemaClass, e);
			throw new ApplicationException(ErrorCode.INVALID_REQUEST);
		}
	}

	private void auditSchemaError(Class<?> schemaClass, String body, String error) {
		CompletableFuture.runAsync(() -> {
			log.info("going to audit schema error [in seperate thread]");
			try {
				this.auditDbService.auditSchemaError(schemaClass, body, error, LocalDateTime.now());
			} catch (Exception e) {
				log.error("exception while saving schema error logs in table", e);
			}

		});
	}

	public String unpretty1(String json) {
		try {
			String[] lines = json.split("\n");
			return Stream.of(lines)
					.map(String::trim)
					.reduce(String::concat)
					.orElseThrow();
		} catch (Exception e) {
			log.error("error in unpretty", e);
		}
		return null;
	}

}
