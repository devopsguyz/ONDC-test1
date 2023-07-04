package com.nsdl.beckn.common.gzip;

import java.util.Map;

import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.codec.EncoderHttpMessageWriter;

import reactor.core.publisher.Mono;

public class GzipHttpMessageWriter extends EncoderHttpMessageWriter {

	public GzipHttpMessageWriter() {
		super(new GzipEncoder());
	}

	@Override
	public Mono<Void> write(Publisher inputStream, ResolvableType elementType, MediaType mediaType, ReactiveHttpOutputMessage message, Map hints) {
		return super.write(inputStream, elementType, mediaType, updateContentEncoding(message), hints);
	}

	private ReactiveHttpOutputMessage updateContentEncoding(ReactiveHttpOutputMessage message) {
		message.getHeaders().add("Content-Encoding", "gzip");
		return message;
	}
}