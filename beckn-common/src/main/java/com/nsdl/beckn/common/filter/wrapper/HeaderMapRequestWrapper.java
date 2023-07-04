package com.nsdl.beckn.common.filter.wrapper;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class HeaderMapRequestWrapper extends HttpServletRequestWrapper {
	public HeaderMapRequestWrapper(HttpServletRequest request) {
		super(request);
	}

	private Map<String, String> headerMap = new HashMap<>();

	public void addHeader(String name, String value) {
		this.headerMap.put(name, value);
	}

	public void removeteHeader(String name) {
		this.headerMap.remove(name);
	}

	@Override
	public String getHeader(String name) {
		String headerValue = super.getHeader(name);
		if (this.headerMap.containsKey(name)) {
			headerValue = this.headerMap.get(name);
		}
		return headerValue;
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		List<String> names = Collections.list(super.getHeaderNames());
		names.addAll(this.headerMap.keySet());
		return Collections.enumeration(names);
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		List<String> values = Collections.list(super.getHeaders(name));
		if (this.headerMap.containsKey(name)) {
			values.add(this.headerMap.get(name));
		}
		return Collections.enumeration(values);
	}

}