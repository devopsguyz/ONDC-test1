package com.nsdl.beckn.common.sender;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.nsdl.beckn.common.exception.ApplicationException;
import com.nsdl.beckn.common.model.SubscriberApiModel;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

// @Component
@Slf4j
public class SenderNoRetry {

	private final WebClient webClient;

	public SenderNoRetry(WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder
				// .filter(WebClientFilter.logRequest())
				// .filter(WebClientFilter.logResponse())
				.build();
	}

	public String send(String url, HttpHeaders headers, String json, SubscriberApiModel apiModel) {
		int timeout = 6_0000; // default timeout
		int retryCount = 0; // default retry count

		if (apiModel != null) {
			timeout = apiModel.getTimeout();
			retryCount = apiModel.getRetryCount();
		}

		log.info("making post request to url {} with timeout {} ms & retryCount {} & headers {}", url, timeout, retryCount, headers);
		Mono<String> response = this.webClient.post()
				.uri(url)
				.headers(h -> {
					h.addAll(headers);
				})
				.contentType(MediaType.APPLICATION_JSON)
				.body(Mono.just(json), String.class)
				.retrieve()
				.onStatus(
						status -> {
							log.info("The http response code is {}", status);
							return status.compareTo(HttpStatus.REQUEST_TIMEOUT) == 0;
						},
						res -> {
							int rawStatusCode = res.rawStatusCode();
							log.error("Error has occured. status code is: {} and reason phrase is {}", rawStatusCode, res.statusCode().getReasonPhrase());
							return Mono.error(new ApplicationException(rawStatusCode, res.statusCode().getReasonPhrase()));
						})
				.bodyToMono(String.class)
				.timeout(Duration.ofMillis(timeout));

		String responseData = response.block();
		log.info("response from post call: {}", responseData);

		return responseData;

	}

}