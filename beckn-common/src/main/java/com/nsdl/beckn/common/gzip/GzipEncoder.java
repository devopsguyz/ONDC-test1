package com.nsdl.beckn.common.gzip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractEncoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class GzipEncoder extends AbstractEncoder<String> {

	public GzipEncoder() {
		super(MediaType.APPLICATION_JSON);
	}

	@Override
	public boolean canEncode(ResolvableType elementType, MimeType mimeType) {
		return MediaType.APPLICATION_JSON.equalsTypeAndSubtype(mimeType) && elementType.isAssignableFrom(String.class);
	}

	@Override
	public DataBuffer encodeValue(String node, DataBufferFactory bufferFactory, ResolvableType valueType, MimeType mimeType, Map<String, Object> hints) {
		return bufferFactory.wrap(gzip(node.toString()));
	}

	@Override
	public Flux<DataBuffer> encode(Publisher<? extends String> inputStream, DataBufferFactory bufferFactory, ResolvableType elementType, MimeType mimeType,
			Map<String, Object> hints) {
		return Flux.from(inputStream).map((String node) -> encodeValue(node, bufferFactory, elementType, mimeType, hints));
	}

	private byte[] gzip(String value) {
		log.info("going to zip the request");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos)) {
			gzipOutputStream.write(value.getBytes());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return baos.toByteArray();
	}
}