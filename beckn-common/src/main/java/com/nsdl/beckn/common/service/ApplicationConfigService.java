/*package com.nsdl.beckn.common.service;

import static com.nsdl.beckn.common.constant.ApplicationConstant.BLANK;
import static com.nsdl.beckn.common.constant.ApplicationConstant.ON;
import static com.nsdl.beckn.common.exception.ErrorCode.SUBSCRIBER_NOT_FOUND;

import java.nio.file.AccessDeniedException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nsdl.beckn.common.cache.CacheApplicationFile;
import com.nsdl.beckn.common.exception.ApplicationException;
import com.nsdl.beckn.common.model.ApiParamModel;
import com.nsdl.beckn.common.model.BlackListModel;
import com.nsdl.beckn.common.model.ConfigModel;
import com.nsdl.beckn.common.model.SigningModel;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ApplicationConfigService {

	@Autowired
	public CacheApplicationFile cacheAppFile;

	public ConfigModel loadApplicationConfiguration(String subscriberId, String apiName) throws AccessDeniedException {

		log.debug("going to load the configuration for subscriberid: {} and api: {}", subscriberId, apiName);

		String action = apiName.trim().toLowerCase();
		action = action.startsWith(ON) ? action.replace(ON, BLANK) : action;

		try {
			List<ConfigModel> configModels = this.cacheAppFile.loadApplicationParentConfiguration().getSubscribers();

			ConfigModel configModel = findConfigBySubscriberId(subscriberId, configModels);

			configModel.setMatchedApi(findMatchingApi(configModel, action));

			log.debug("The content of init config file based on subscriberId[{}]  is {}", subscriberId, configModel);
			return configModel;
		} catch (Exception e) {
			String error = "not able to load the configuration for subscriberid " + subscriberId + " and api " + action;
			log.error(error);
			throw new AccessDeniedException("subscriber[" + subscriberId + "] not configured");
		}

	}

	public ConfigModel loadApplicationConfigurationByUrlId(String urlId, String apiName) throws AccessDeniedException {

		log.info("going to load the configuration for urlId: {} and api: {}", urlId, apiName);

		String action = apiName.trim().toLowerCase();
		action = action.startsWith(ON) ? action.replace(ON, BLANK) : action;

		try {
			List<ConfigModel> configModels = this.cacheAppFile.loadApplicationParentConfiguration().getSubscribers();

			ConfigModel configModel = findConfigByUrlId(urlId, configModels);

			configModel.setMatchedApi(findMatchingApi(configModel, action));

			log.info("The content of init config file based on urlId[{}] is {}", urlId, configModel);
			return configModel;
		} catch (Exception e) {
			log.error("error while loading config json file", e);
			String error = "not able to load the configuration for urlId " + urlId + " and api " + action;
			log.error(error);
			// throw new RuntimeException(error);
			throw new AccessDeniedException("urlId[" + urlId + "] not configured");
		}

	}

	public SigningModel getSigningConfiguration(String subscriberId) {
		log.debug("going to load the config file for signing for subscriberid: {}", subscriberId);
		try {
			List<ConfigModel> configModels = this.cacheAppFile.loadApplicationParentConfiguration().getSubscribers();

			ConfigModel configModel = findConfigBySubscriberId(subscriberId, configModels);

			log.debug("The content of init config file for signing config is {}", configModel);
			return configModel.getSigning();
		} catch (Exception e) {
			log.error("error while loading sign configuration", e);
			throw new RuntimeException("Exception while loading the config json file");
		}
	}

	public BlackListModel getBacklistConfiguration(String subscriberId) {
		log.debug("going to load the config file to get backlist urls");
		try {
			BlackListModel blacklist = this.cacheAppFile.loadApplicationParentConfiguration().getBlacklist();
			log.debug("The content of init config file for blacklist urls is {}", blacklist);
			return blacklist;
		} catch (Exception e) {
			log.error("error while loading blacklist configuration", e);
			throw new RuntimeException("Exception while loading the config json file");
		}
	}

	private ConfigModel findConfigBySubscriberId(String subscriberId, List<ConfigModel> configModels) {
		ConfigModel configModel = configModels
				.stream()
				.filter(model -> model.getSubscriberId().equalsIgnoreCase(subscriberId))
				.findFirst()
				.orElseThrow(() -> {
					String error = "not able to find the subscriberId [" + subscriberId + "] in the config json file";
					throw new ApplicationException(SUBSCRIBER_NOT_FOUND, error);
				});

		log.info("the matched config model loaded for the subscriberId {}", subscriberId);
		log.debug("the matched config model for the subscriberId {} is {}", subscriberId, configModel);
		return configModel;
	}

	private ConfigModel findConfigByUrlId(String urlId, List<ConfigModel> configModels) {
		ConfigModel configModel = configModels
				.stream()
				.filter(model -> model.getShortName().equalsIgnoreCase(urlId))
				.findFirst()
				.orElseThrow(() -> {
					String error = "not able to find the urlId [" + urlId + "] in the config json file";
					throw new ApplicationException(SUBSCRIBER_NOT_FOUND, error);
				});

		log.info("the matched config model for the urlId {} is {}", urlId, configModel);
		return configModel;
	}

	private ApiParamModel findMatchingApi(ConfigModel configModel, String apiName) {
		return configModel.getApi()
				.stream()
				.filter(model -> apiName.equalsIgnoreCase(model.getName()))
				.findFirst()
				.orElseThrow(() -> {
					throw new RuntimeException("Invalid API name configured. Please configure json file correctly");
				});

	}

}
*/
