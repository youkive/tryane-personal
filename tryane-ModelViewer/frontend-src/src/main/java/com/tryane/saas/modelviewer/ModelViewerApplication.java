package com.tryane.saas.modelviewer;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import com.tryane.saas.modelviewer.config.CommonDatabaseConfig;


@SpringBootApplication
public class ModelViewerApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		new ModelViewerApplication().configure(new SpringApplicationBuilder(ModelViewerApplication.class).sources(CommonDatabaseConfig.class)).run(args);
	}
}
