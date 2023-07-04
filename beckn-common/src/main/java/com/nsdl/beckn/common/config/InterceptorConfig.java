package com.nsdl.beckn.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.nsdl.beckn.common.interceptor.HeaderValidationInterceptor;

// @Configuration
public class InterceptorConfig implements WebMvcConfigurer {

	@Autowired
	private HeaderValidationInterceptor headerValidationInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(this.headerValidationInterceptor);
	}
}
