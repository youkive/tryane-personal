package com.tryane.saas.personal.model.viewer;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

@Component
@ApplicationPath("api/v1")
public class JerseyConfig extends ResourceConfig {

	public JerseyConfig() {
		packages("com.tryane.saas.personal.model.viewer.model");
		register(JacksonJsonProvider.class);
	}
}
