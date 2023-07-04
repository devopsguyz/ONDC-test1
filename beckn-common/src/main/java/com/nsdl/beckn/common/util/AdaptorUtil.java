package com.nsdl.beckn.common.util;

import static com.nsdl.beckn.common.constant.ApplicationConstant.DB_POSTGRES;
import static com.nsdl.beckn.common.constant.ApplicationConstant.FILE;
import static com.nsdl.beckn.common.constant.ApplicationConstant.HTTP;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AdaptorUtil {

	@Value("${beckn.persistence.type}")
	private String persistenceTypes;

	public boolean isDataBasePersistanceConfigured() {
		String[] types = this.persistenceTypes.split("\\|");
		if (types != null && types.length > 0) {
			for (String type : types) {
				if (DB_POSTGRES.equals(type.trim())) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isFilePersistanceConfigured() {
		String[] types = this.persistenceTypes.split("\\|");
		if (types != null && types.length > 0) {
			for (String type : types) {
				if (FILE.equals(type.trim())) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isHttpPersistanceConfigured() {
		String[] types = this.persistenceTypes.split("\\|");
		if (types != null && types.length > 0) {
			for (String type : types) {
				if (HTTP.equals(type.trim())) {
					return true;
				}
			}
		}
		return false;
	}
}
