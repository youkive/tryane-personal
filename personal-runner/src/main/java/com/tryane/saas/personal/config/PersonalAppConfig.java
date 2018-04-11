package com.tryane.saas.personal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan(basePackages = "com.tryane.saas")
//@PropertySource - the last definition will win and override the previous ones.
@PropertySource(ignoreResourceNotFound = true, value = { "classpath:/app.properties", "classpath:/connector.sharepoint.utils.properties", "file:${SAAS_HOME}/conf/saas.properties" })
//@EnableAspectJAutoProxy(proxyTargetClass = true)
public class PersonalAppConfig {

	/* Permet d'utiliser les annotations @Value et les expressions ${}
	 * voir http://docs.spring.io/spring/docs/3.1.x/javadoc-api/org/springframework/context/support/PropertySourcesPlaceholderConfigurer.html */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
}