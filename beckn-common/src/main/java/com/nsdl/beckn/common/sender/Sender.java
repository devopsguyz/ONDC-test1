package com.nsdl.beckn.common.sender;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.nsdl.beckn.common.exception.ApplicationException;
import com.nsdl.beckn.common.exception.ErrorCode;
import com.nsdl.beckn.common.model.SubscriberApiModel;
import com.nsdl.beckn.common.service.CommonService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
@Slf4j
public class Sender {

	@Autowired
	private WebClient webClient;

	@Autowired
	@Qualifier("gzipWebClient")
	private WebClient gzipWebClient;

	@Autowired
	private CommonService commonService;

	@Value("${beckn.http.timeout: 30000}")
	private int defaultTimeout;

	@Value("${beckn.http.backoff: 1000}")
	private int backoff;

	@Value("${beckn.http.retry-count: 0}")
	private int defaultRetryCount;

	@Value("${beckn.gzip.enabled.outgoing: false}")
	private boolean gzipEnabled;

	@Value("${beckn.gzip.actions:}")
	private List<String> gzipActions;

	public String send(String url, HttpHeaders headers, String json, SubscriberApiModel apiModel) {
		int timeout;
		int retryCount;
		final Instant start = Instant.now();

		if (apiModel == null || apiModel.getTimeout() <= 0) {
			timeout = this.defaultTimeout;
		} else {
			timeout = apiModel.getTimeout();
		}

		if (apiModel == null || apiModel.getRetryCount() < 0) {
			retryCount = this.defaultRetryCount;
		} else {
			retryCount = apiModel.getRetryCount();
		}

		if (isBlank(url) || url.split("\\/").length <= 1) {
			log.error("invalid url {}", url);
			return null;
		}

		log.info("calculating content length");
		float length = (float) json.getBytes(Charset.forName("UTF-8")).length / 1000;

		log.info("making post call[content length {} kb] to url {} with timeout {} ms and retryCount {} & header {}", length, url, timeout, retryCount,
				headers);

		WebClient client = this.webClient;

		if (this.gzipEnabled) {
			log.info("gzip is enabled");
			String[] split = url.split("\\/");
			String action = split[split.length - 1];
			if (this.gzipActions.contains(action.trim())) {
				log.info("GzipWebClient will be used to make post call");
				client = this.gzipWebClient;
			}
		}

		Mono<String> response = client.post()
				.uri(url)
				.headers(h -> {
					h.addAll(headers);
				})
				.contentType(MediaType.APPLICATION_JSON)
				.body(Mono.just(json), String.class)
				.retrieve()
				.onStatus(
						status -> {
							log.info("The http response code is {} from calling url [{}]", status, url);
							return status.compareTo(HttpStatus.REQUEST_TIMEOUT) == 0;
						},
						res -> {
							int rawStatusCode = res.rawStatusCode();
							log.error("Error has occured while calling url {}. status code is {} & reason phrase is {}", url, rawStatusCode, res.statusCode()
									.getReasonPhrase());
							return Mono.error(new ApplicationException(rawStatusCode, res.statusCode().getReasonPhrase()));
						})
				.bodyToMono(String.class)
				.timeout(Duration.ofMillis(timeout))
				.retryWhen(Retry
						.backoff(retryCount, Duration.ofMillis(this.backoff))
						.doAfterRetry(retrySignal -> {
							log.info("Retried {} for url {}", retrySignal.totalRetries(), url);
						})
						.filter(throwable -> {
							log.error("Exception has occured while calling url [{}] : {}", url, throwable.getMessage());
							return throwable instanceof TimeoutException;
						}).onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
							log.error("All retry exhaused calling url {}", url);
							return new ApplicationException(ErrorCode.HTTP_TIMEOUT_ERROR);
						}));

		String responseData = response.block();
		log.info("response received in {} ms from post call to url [{}]", this.commonService.getTimeElapsed(start), url);
		log.debug("response received from post call [{}]: {}", url, responseData);

		return responseData;

	}

}
