package com.tryane.saas.toolsv2.config;

import javax.ws.rs.ext.ContextResolver;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;

public class JacksonMapperProvider implements ContextResolver<ObjectMapper> {

	public static final ObjectMapper DEFAULT_OBJECT_MAPPER;

	static {
		DEFAULT_OBJECT_MAPPER = new ObjectMapper();
		DEFAULT_OBJECT_MAPPER.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
		DEFAULT_OBJECT_MAPPER.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
		DEFAULT_OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		DEFAULT_OBJECT_MAPPER.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		DEFAULT_OBJECT_MAPPER.registerModule(new JodaModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
	}

	public JacksonMapperProvider() {
	}

	@Override
	public ObjectMapper getContext(Class<?> type) {
		return DEFAULT_OBJECT_MAPPER;
	}

}
