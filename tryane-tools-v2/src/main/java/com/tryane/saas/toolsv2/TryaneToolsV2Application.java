package com.tryane.saas.toolsv2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@ComponentScan("com.tryane.saas.toolsv2")
@PropertySource(ignoreResourceNotFound = true, value = { "classpath:/app.properties" })
public class TryaneToolsV2Application {

	public static void main(String[] args) {
		SpringApplication.run(TryaneToolsV2Application.class, args);
	}

}
