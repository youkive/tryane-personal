package com.tryane.saas.personal.sharepoint.missingitem;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.connector.common.manager.collaboratorid.IIDManager;
import com.tryane.saas.connector.common.manager.context.IConnectorContextInitialiser;
import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.connector.o365.utils.exception.O365UserAuthenticationException;
import com.tryane.saas.connector.o365.utils.token.IAppTokenManager;
import com.tryane.saas.connector.sharepoint.process.item.ISPItemUpdaterProcess;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.connector.execution.ConnectorExecution;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.NetworkType;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class SpItemUpdaterProcessRunner extends AbstractSpringRunner {

	private static final Logger				LOGGER		= LoggerFactory.getLogger(SpItemUpdaterProcessRunner.class);

	private static final String				NETWORK_ID	= "s11";

	@Autowired
	private INetworkManager					networkManager;

	@Autowired
	private INetworkPropertyManager			networkPropertyManager;

	@Autowired
	private ISPItemUpdaterProcess			spItemUpdater;

	@Autowired
	private IAppTokenManager				appTokenManager;

	@Autowired
	private IConnectorContextInitialiser	connectorContextInitialiser;

	@Autowired
	private IIDManager						idService;

	@Override
	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));
		String tenantId = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_TENANT);

		ConnectorExecution connectorExecution = new ConnectorExecution();
		connectorExecution.setExecutionDay(LocalDate.now());
		connectorExecution.setConnectorType(NetworkType.SHAREPOINT);
		connectorExecution.setNetworkId("s11");
		connectorContextInitialiser.initConnectorContext(LocalDate.now().minusDays(1), LocalDate.now().minusDays(1), connectorExecution);
		appTokenManager.initForTenant(tenantId);
		idService.initForCurrentNetwork();

		try {
			spItemUpdater.updateItems(tenantId);
		} catch (O365UserAuthenticationException | O365ConnectionException | O365HttpErrorException | InterruptedException e) {
			LOGGER.error("", e);
		}
	}

	public static void main(String[] args) {
		new SpItemUpdaterProcessRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}
}
