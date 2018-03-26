package com.tryane.saas.personal.aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.tryane.saas.core.aspect.monitoring.PerfMonitoring;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class AspectRunner {

	public static Logger				LOGGER	= LoggerFactory.getLogger(AspectRunner.class);

	@Autowired
	private ComponentUnderMonitoring	componentUnderMonitoring;

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, PersonalDatabaseConfig.class);

			AspectRunner runner = new AspectRunner();
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);
			ctx.getAutowireCapableBeanFactory().initializeBean(runner, "monBean");

			runner.execute();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	public void execute() {
		try {
			methodUnderMonitoring();
		} catch (InterruptedException e) {
			LOGGER.error("", e);
		}
	}

	@PerfMonitoring
	public void methodUnderMonitoring() throws InterruptedException {
		LOGGER.info("passe dans ma method underMonitoring");
		componentUnderMonitoring.methodUnderMonitoring();
	}
}
