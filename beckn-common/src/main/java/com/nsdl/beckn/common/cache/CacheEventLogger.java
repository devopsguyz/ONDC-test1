package com.nsdl.beckn.common.cache;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CacheEventLogger implements CacheEventListener<Object, Object> {

	@Override
	public void onEvent(CacheEvent<?, ?> event) {
		log.info("EventType: {} ## Key: {} ## Old Value: {} ## New Value {}", event.getType(), event.getKey(), event.getOldValue(), event.getNewValue());
	}

}
