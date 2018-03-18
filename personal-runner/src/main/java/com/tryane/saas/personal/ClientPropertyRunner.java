package com.tryane.saas.personal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.tryane.saas.core.client.ClientPropertyNames;
import com.tryane.saas.core.client.properties.IClientPropertyManager;
import com.tryane.saas.db.updater.config.DatabaseConfig;
import com.tryane.saas.personal.config.PersonalAppConfig;

public class ClientPropertyRunner {

	private static final Long		CLIENT_ID		= 1L;

	private static final String		PROPERTY_NAME	= ClientPropertyNames.ACCOUNT_CLOSED_DATE;

	private static final String		VALUE			= "2017-11-01";

	@Autowired
	private IClientPropertyManager	clientpropertyManager;

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, DatabaseConfig.class);
			ClientPropertyRunner runner = new ClientPropertyRunner();
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);

			runner.execute();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	public void execute() {
		clientpropertyManager.setClientPropertyValue(CLIENT_ID, PROPERTY_NAME, VALUE);
	}
}
