package com.nsdl.beckn.common.config;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.nsdl.beckn.common.gzip.GzipHttpMessageWriter;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.resolver.DefaultAddressResolverGroup;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
@EnableCaching
@EnableAsync
@EnableScheduling
@Slf4j
public class AppConfig {

	@Value("${beckn.thread.common}")
	private int commonThreadCount;

	@Value("${beckn.thread.http}")
	private int httpThreadCount;

	@Value("${beckn.thread.database}")
	private int databaseThreadCount;

	@Value("${beckn.webflux.codec.max-in-memory-size}")
	private int maxInMemorySize;

	@Value("${beckn.webflux.provider.max-connections}")
	private int maxConnections;

	@Value("${beckn.webflux.provider.max-idle-time}")
	private int maxIdleTime;

	@Value("${beckn.webflux.provider.max-life-time}")
	private int maxLifeTime;

	@Value("${beckn.webflux.provider.pending-acquire-timeout}")
	private int pendingAcquireTimeout;

	@Value("${beckn.webflux.provider.evict-in-background}")
	private int evictInBackground;

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	@Qualifier("snakeCaseObjectMapper")
	ObjectMapper jacksonObjectMapper() {
		return JsonMapper.builder()
				.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
				.serializationInclusion(Include.NON_NULL)
				.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
				.findAndAddModules()
				.build();
	}

	@Bean
	@Qualifier("gzipWebClient")
	WebClient gzipWebClient() throws SSLException {
		System.out.println("loading gzipWebClient.....");

		SslContext sslContext = SslContextBuilder.forClient()
				.trustManager(InsecureTrustManagerFactory.INSTANCE)
				.build();

		ConnectionProvider provider = ConnectionProvider.builder("fixed")
				.maxConnections(500)
				.maxIdleTime(Duration.ofSeconds(20))
				.maxLifeTime(Duration.ofSeconds(60))
				.pendingAcquireTimeout(Duration.ofSeconds(60))
				.evictInBackground(Duration.ofSeconds(120)).build();

		HttpClient httpConnector = HttpClient
				.create(provider)
				.resolver(DefaultAddressResolverGroup.INSTANCE)
				.secure(t -> t.sslContext(sslContext))
				.wiretap(true);

		return WebClient.builder()
				.codecs(clientCodecConfigurer -> clientCodecConfigurer.customCodecs().register(new GzipHttpMessageWriter()))
				.clientConnector(new ReactorClientHttpConnector(httpConnector))
				.build();
	}

	@Bean
	@Primary
	WebClient webClient() throws SSLException {
		logInfo();

		SslContext sslContext = SslContextBuilder.forClient()
				.trustManager(InsecureTrustManagerFactory.INSTANCE)
				.build();

		ConnectionProvider provider = ConnectionProvider.builder("fixed")
				.maxConnections(this.maxConnections)
				.maxIdleTime(Duration.ofSeconds(this.maxIdleTime))
				.maxLifeTime(Duration.ofSeconds(this.maxLifeTime))
				.pendingAcquireTimeout(Duration.ofSeconds(this.pendingAcquireTimeout))
				.evictInBackground(Duration.ofSeconds(this.evictInBackground))
				.build();

		HttpClient httpConnector = HttpClient
				.create(provider)
				.resolver(DefaultAddressResolverGroup.INSTANCE)
				.secure(t -> t.sslContext(sslContext));

		return WebClient.builder()
				.exchangeStrategies(ExchangeStrategies.builder()
						.codecs(configurer -> configurer
								.defaultCodecs()
								.maxInMemorySize(this.maxInMemorySize))
						.build())
				.clientConnector(new ReactorClientHttpConnector(httpConnector))
				.build();
	}

	@Bean("commonExecutor")
	Executor commonTaskExecutor() {
		log.warn("CommonExecutor thread count is {}", this.commonThreadCount);
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(this.commonThreadCount);
		executor.setAllowCoreThreadTimeOut(true);
		executor.setMaxPoolSize(Integer.MAX_VALUE);
		executor.setQueueCapacity(Integer.MAX_VALUE);
		executor.setThreadNamePrefix("task-common-");
		executor.initialize();
		return executor;
	}

	@Bean("httpExecutor")
	Executor httpTaskExecutor() {
		log.warn("HttpExecutor thread count is {}", this.httpThreadCount);
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(this.httpThreadCount);
		executor.setAllowCoreThreadTimeOut(true);
		executor.setMaxPoolSize(Integer.MAX_VALUE);
		executor.setQueueCapacity(Integer.MAX_VALUE);
		executor.setThreadNamePrefix("task-http-");
		executor.initialize();
		return executor;
	}

	@Bean("dbExecutor")
	Executor dbTaskExecutor() {
		log.warn("DBExecutor thread count is {}", this.databaseThreadCount);
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(this.databaseThreadCount);
		executor.setAllowCoreThreadTimeOut(true);
		executor.setMaxPoolSize(Integer.MAX_VALUE);
		executor.setQueueCapacity(Integer.MAX_VALUE);
		executor.setThreadNamePrefix("task-db-");
		executor.initialize();
		return executor;
	}

	// @Bean("dbExecutor")
	Executor databaseTaskExecutor() {
		return Executors.newFixedThreadPool(100);
	}

	private void logInfo() {
		System.out.println("loading WebClient with webflux codec maxInMemorySize: " + this.maxInMemorySize);
		log.warn(
				"loading WebClient with maxConnections[{}], maxIdleTime[{}], maxLifeTime[{}], pendingAcquireTimeout[{}], evictInBackground[{}], codec.maxInMemorySize[{}],",
				this.maxConnections, this.maxIdleTime, this.maxLifeTime, this.pendingAcquireTimeout, this.evictInBackground, this.maxInMemorySize);
	}

}
