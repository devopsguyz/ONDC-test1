package com.nsdl.beckn.common.config;

import static com.nsdl.beckn.common.constant.ApplicationConstant.ALLOWED_PERSISTENCE_TYPES;
import static com.nsdl.beckn.common.constant.ApplicationConstant.DASH;
import static com.nsdl.beckn.common.constant.ApplicationConstant.DB_POSTGRES;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import com.nsdl.beckn.common.model.yml.YmlApplicationModel;
import com.nsdl.beckn.common.model.yml.YmlPersistence;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PostgresDatabaseTypeCondition extends SpringBootCondition {

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
		boolean database = false;
		try {
			String profile = context.getEnvironment().getProperty("spring.profiles.active");
			log.info("spring profile is {}", profile);
			final String fileName = "application" + DASH + profile + ".yml";

			/*
			 * ClassLoader loader = Thread.currentThread().getContextClassLoader();
			 * InputStream is = loader.getResourceAsStream(fileName);
			 */

			Resource resource = null;
			InputStream is = null;
			try {
				resource = new ClassPathResource(fileName);
				is = resource.getInputStream();
			} catch (Exception e) {
				log.warn("going to look for file in current directory");
				// creates a file object
				File file = new File("./");

				// returns an array of all files
				String[] fileList = file.list();

				for (String name : fileList) {
					log.info("file: {}", name);
				}
				String newPath = "./" + fileName;
				log.info("looking for file at path {}", newPath);
				is = new FileInputStream(newPath);
			}

			log.info("file {} loaded to read the configurations for database persistence", fileName);

			Representer representer = new Representer();
			representer.getPropertyUtils().setSkipMissingProperties(true);
			Yaml yaml = new Yaml(new Constructor(YmlApplicationModel.class), representer);
			YmlApplicationModel model = yaml.load(is);
			log.warn("Yml ApplicationModel is {}", model);

			YmlPersistence persistence = model.getBeckn().getPersistence();

			if (persistence != null && StringUtils.isNotBlank(persistence.getType())) {
				String[] types = persistence.getType().split("\\|");
				if (types != null && types.length > 0) {
					database = validateAndFindIfDbPersistence(types);
				}
			}

		} catch (Exception e) {
			log.error("error in getMatchOutcome", e);
		}
		System.out.println("in ConditionOutcome................");
		if (database) {
			return ConditionOutcome.noMatch("No database configuration required");
		}
		return ConditionOutcome.match();
	}

	private boolean validateAndFindIfDbPersistence(String[] types) {
		for (String type : types) {
			log.info("Checking persistence type {}", type);
			if (!ALLOWED_PERSISTENCE_TYPES.contains(type)) {
				throw new RuntimeException("Invalid persistence type configured");
			}
			if (DB_POSTGRES.equals(type)) {
				return true;
			}
		}

		return false;
	}

}