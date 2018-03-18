package com.tryane.saas.personal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.google.common.base.Strings;
import com.tryane.saas.core.client.Client;
import com.tryane.saas.core.client.ClientPropertyNames;
import com.tryane.saas.core.client.IClientManager;
import com.tryane.saas.core.client.properties.IClientPropertyManager;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class SearchClientRunner {

	private static final Logger		LOGGER	= LoggerFactory.getLogger(SearchClientRunner.class);

	@Autowired
	private IClientManager			clientManager;

	@Autowired
	private IClientPropertyManager	clientPropertyManager;

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, PersonalDatabaseConfig.class);
			SearchClientRunner runner = new SearchClientRunner();
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);

			runner.execute();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	public void execute() {
		searchClientWithTenant("195c2309-d731-4bfe-98ce-ea7b0bf5a899");
	}

	public void searchClientWithTenant(String tenantToSearch) {
		clientManager.getAllClientIds().forEach(clientId -> {
			String tenantId = clientPropertyManager.getClientPropertyValue(clientId, ClientPropertyNames.O365_TENANT_ID);
			if (Strings.isNullOrEmpty(tenantId)) {
				return;
			} else if (tenantId.equals(tenantToSearch)) {
				displayClientInfo(clientId);
			}
		});
	}

	public void displayClientInfo(Long clientId) {
		Client client = clientManager.getClientById(clientId);
		LOGGER.info("clientID : {}", clientId);
		LOGGER.info("client name : {}", client.getClientName());
		LOGGER.info("");
	}
}
