package com.tryane.saas.personal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

abstract class AbstractSpringRunner {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSpringRunner.class);

	protected abstract void testImplementation();

	public void runTest(String springProfile, Class<?>... configClasses) {
		System.setProperty("spring.profiles.active", springProfile);

		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext(configClasses);
			ctx.getBeanFactory().autowireBean(this);
			this.testImplementation();

		} catch (Exception e) {
			LOGGER.error("", e);
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}
}
