package com.nsdl.beckn.common.filter;

import static com.nsdl.beckn.common.constant.ApplicationConstant.BLANK;
import static com.nsdl.beckn.common.constant.ApplicationConstant.REMOTE_HOST;
import static com.nsdl.beckn.common.exception.ErrorCodeOndc.GATEWAY_10000;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nsdl.beckn.api.model.common.Context;
import com.nsdl.beckn.api.model.object.ObjectRequest;
import com.nsdl.beckn.common.builder.ResponseBuilder;
import com.nsdl.beckn.common.filter.wrapper.DecompressServletInputStream;
import com.nsdl.beckn.common.filter.wrapper.HeaderMapRequestWrapper;
import com.nsdl.beckn.common.filter.wrapper.StreamHelper;
import com.nsdl.beckn.common.util.JsonUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
class RequestWrapperFilter extends OncePerRequestFilter {

	private static final String POSTMAN_RUNTIME = "PostmanRuntime";
	private static final String X_FORWARDED_FOR = "x-forwarded-for";

	@Autowired
	private ResponseBuilder responseBuilder;

	@Autowired
	private JsonUtil jsonUtil;

	@Value("${beckn.parameter.block-postman: false}")
	private boolean blockPostman;

	@Value("${beckn.gzip.enabled.incoming: false}")
	private boolean gzipEnabled;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		Map<String, String> headerMap = collectRequestHeaders(request);

		String remoteAddr = BLANK;

		if (request != null) {
			remoteAddr = request.getHeader(X_FORWARDED_FOR);
			if (isBlank(remoteAddr)) {
				remoteAddr = request.getRemoteAddr();
			}
			if (isBlank(remoteAddr)) {
				remoteAddr = request.getRemoteHost();
			}
		}

		// changes for unzip
		String contentEncoding = request.getHeader("content-encoding");
		if (this.gzipEnabled && contentEncoding != null && contentEncoding.indexOf("gzip") > -1) {
			request = unzip(request);
			log.info("unzip completed");
		}
		// end of changes for unzip

		HeaderMapRequestWrapper wrappedRequest = new HeaderMapRequestWrapper(request);
		wrappedRequest.addHeader(REMOTE_HOST, remoteAddr);
		// wrappedRequest.addHeader(ALL_REQUEST_HEADERS, headerMap.toString());

		log.info("call is from host: {}", wrappedRequest.getHeader(REMOTE_HOST));

		// check for postman
		String userAgent = request.getHeader("user-agent");
		if (this.blockPostman && isNotBlank(userAgent) && userAgent.toLowerCase().contains(POSTMAN_RUNTIME.toLowerCase())) {
			log.error("PostmanRuntime found in header & its blocked in the configuration");
			buildPostmanErrorResponse(request, response);
			return;
		}

		filterChain.doFilter(wrappedRequest, response);
	}

	private HttpServletRequest unzip(HttpServletRequest request) {
		log.info("gzip is enabled and header found, going to uzip and write it to the response");
		try {
			final InputStream decompressStream = StreamHelper.decompressStream(request.getInputStream());

			request = new HttpServletRequestWrapper(request) {

				@Override
				public ServletInputStream getInputStream() throws IOException { return new DecompressServletInputStream(decompressStream); }

				@Override
				public BufferedReader getReader() throws IOException { return new BufferedReader(new InputStreamReader(decompressStream)); }
			};
		} catch (Exception e) {
			log.error("error while decompressing the stream in the request", e);
		}
		return request;
	}

	private void buildPostmanErrorResponse(HttpServletRequest request, HttpServletResponse response) throws IOException, JsonProcessingException {
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType(MediaType.APPLICATION_JSON.toString());

		String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

		ObjectRequest modelObject = this.jsonUtil.toModel(body, ObjectRequest.class);
		Context ctx = modelObject.getContext();

		String error = this.responseBuilder.buildNotAckResponse(ctx, GATEWAY_10000, "The request from postman is not allowed");
		response.getOutputStream().write(error.getBytes());
		log.info("error response written to servlet response");
	}

	private Map<String, String> collectRequestHeaders(HttpServletRequest request) {

		Map<String, String> result = new HashMap<>();

		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String key = headerNames.nextElement();
			String value = request.getHeader(key);
			result.put(key, value);

			log.debug("{}={}", key, value);
		}

		log.info("headers are {}", result);

		return result;
	}
}