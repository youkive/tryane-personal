package com.tryane.saas.toolsv2.config;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
@ApplicationPath("/api/v1")
public class JerseyConfig extends ResourceConfig {

	public JerseyConfig() {
		packages("com.tryane.saas.toolsv2.api.v1");

		register(JacksonMapperProvider.class);
	}
}
