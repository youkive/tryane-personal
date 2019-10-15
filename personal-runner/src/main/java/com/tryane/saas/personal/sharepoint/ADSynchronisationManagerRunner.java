package com.tryane.saas.personal.sharepoint;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.tryane.saas.connector.graph.manager.IADSynchronizationManager;
import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.connector.o365.utils.exception.O365UserAuthenticationException;
import com.tryane.saas.connector.o365.utils.token.IAppTokenManager;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class ADSynchronisationManagerRunner {

	private static final Logger			LOGGER		= LoggerFactory.getLogger(ADSynchronisationManagerRunner.class);

	private final String				NETWORK_ID	= "s1";

	@Autowired
	private INetworkManager				networkManager;

	@Autowired
	private INetworkPropertyManager		networkPropertyManager;

	@Autowired
	private IAppTokenManager			appTokenManager;

	@Autowired
	private IADSynchronizationManager	adSynchronisationManager;

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "dev");

		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, PersonalDatabaseConfig.class);
			ADSynchronisationManagerRunner runner = new ADSynchronisationManagerRunner();
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);
			runner.execute();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	public void execute() {
		Network network = networkManager.getNetworkById(NETWORK_ID);
		ClientContextHolder.setNetwork(network);

		String tenantId = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_TENANT);
		try {
			appTokenManager.initForTenant(tenantId);
			adSynchronisationManager.synchronizeADWithNetwork(LocalDate.now(), false);
		} catch (O365ConnectionException | O365HttpErrorException | O365UserAuthenticationException e) {
			LOGGER.error("", e);
		} finally {
			appTokenManager.clearForTenant(tenantId);
		}
	}
}
