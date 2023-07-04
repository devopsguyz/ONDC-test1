/*package com.nsdl.beckn.common.cache;

import static com.nsdl.beckn.common.constant.ApplicationConstant.BECKN_API_COMMON_CACHE;
import static com.nsdl.beckn.common.constant.ApplicationConstant.DASH;

import java.io.FileInputStream;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nsdl.beckn.common.model.ConfigModelParent;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CacheApplicationFile {

	@Autowired
	public Environment env;

	@Autowired
	@Value("classpath:config-${beckn.entity.type}-${spring.profiles.active}.json")
	private Resource resource;

	@Autowired
	private ObjectMapper mapper;

	public void loadConfigurationOnStartup() {
		loadApplicationParentConfiguration();
	}

	@Cacheable(value = BECKN_API_COMMON_CACHE)
	public ConfigModelParent loadApplicationParentConfiguration() {

		log.info("going to load the parent configuration");

		InputStream is = null;
		try {
			is = this.resource.getInputStream();
		} catch (Exception e) {
			try {
				String newPath = "./config-" + this.env.getProperty("beckn.entity.type") + DASH + this.env.getProperty("spring.profiles.active") + ".json";
				log.info("looking for file at path {}", newPath);
				is = new FileInputStream(newPath);
			} catch (Exception e1) {
				throw new RuntimeException("Exception while loading the config json file {}", e1);
			}
		}

		try {
			ConfigModelParent configModelParent = this.mapper.readValue(is, ConfigModelParent.class);

			log.info("config parent model loaded {}", configModelParent);

			return configModelParent;
		} catch (Exception e) {
			throw new RuntimeException("Exception while loading the config json file", e);
		}

	}

}
*/
