package com.tryane.saas.personal.aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.tryane.saas.core.aspect.monitoring.PerfMonitoring;

@Component("componentUnderMonitoring")
public class ComponentUnderMonitoring {

	public final Logger LOGGER = LoggerFactory.getLogger(ComponentUnderMonitoring.class);

	@PerfMonitoring
	public void methodUnderMonitoring() throws InterruptedException {
		Thread.sleep(2000);
		LOGGER.info("component:methodUnderMonitoring");
	}
}
