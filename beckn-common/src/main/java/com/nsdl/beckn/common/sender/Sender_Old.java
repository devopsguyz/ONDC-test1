package com.nsdl.beckn.common.sender;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.nsdl.beckn.common.exception.ApplicationException;
import com.nsdl.beckn.common.exception.ErrorCode;
import com.nsdl.beckn.common.model.SubscriberApiModel;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
public class Sender_Old {

	private final WebClient webClient;

	public Sender_Old(WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder
				// .filter(WebClientFilter.logRequest())
				// .filter(WebClientFilter.logResponse())
				.build();
	}

	public String send(String url, HttpHeaders headers, String json, SubscriberApiModel apiModel) {
		int timeout = 30_000; // default timeout
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
				.timeout(Duration.ofMillis(timeout))
				.retryWhen(Retry
						.backoff(retryCount, Duration.ofSeconds(1))
						.doAfterRetry(retrySignal -> {
							log.info("Retried " + retrySignal.totalRetries());
						})
						.filter(throwable -> {
							log.error("Excpetion has occured while sending request: {}", throwable.getMessage());
							return throwable instanceof TimeoutException;
						}).onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
							log.error("All retry exhaused");
							return new ApplicationException(ErrorCode.HTTP_TIMEOUT_ERROR);
						}));

		String responseData = response.block();
		log.info("response from post call: {}", responseData);

		return responseData;

	}

	public String send1(String url, HttpHeaders headers, String json, SubscriberApiModel apiModel) {
		int timeout = 30_000; // default timeout
		int retryCount = 0; // default retry count

		if (apiModel != null) {
			timeout = apiModel.getTimeout();
			retryCount = apiModel.getRetryCount();
		}

		log.info("making post request to url {} with timeout {} ms and retryCount {}", url, timeout, retryCount);
		try {
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
					.timeout(Duration.ofMillis(timeout))
					.retryWhen(Retry
							.backoff(retryCount, Duration.ofSeconds(1))
							.filter(throwable -> {
								log.error("Exception has occured while sending request: {}", throwable.getMessage());
								return throwable instanceof TimeoutException;
							})
							.onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
								log.error("All retry exhaused");
								return new TimeoutException();
							}));

			String responseData = response.block();
			log.info("response from post call: {}", responseData);

			return responseData;

		} catch (WebClientResponseException we) {
			log.error("WebClientResponseException while calling the post at url {}", url);
			log.error("WebClientResponseException is {}", we.getMessage());
			// throw new ApplicationException(we.getRawStatusCode(), we.getMessage());
			throw new ApplicationException(we.getRawStatusCode(), we.getMessage());
		} catch (WebClientRequestException e) {
			log.error("WebClientRequestException while calling the post at url {}", url);
			log.error("WebClientRequestException is {}", e.getMessage());
			throw new ApplicationException(ErrorCode.NETWORK_ERROR, e.getMessage());
		} catch (Exception e) {
			log.error("Exception while calling the post at url {}", url);
			log.error("Exception is {}", e.getMessage());
			throw new ApplicationException(ErrorCode.NETWORK_ERROR, e.getMessage());
		}

	}

}
