package com.tryane.saas.personal.sharepoint.sitecollections;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.connector.common.manager.collaboratorid.IIDManager;
import com.tryane.saas.connector.common.manager.context.IConnectorContextInitialiser;
import com.tryane.saas.connector.o365.utils.token.AppTokenManager;
import com.tryane.saas.connector.o365.utils.token.IAppTokenManager;
import com.tryane.saas.connector.sharepoint.manager.sitecollection.ISPSiteCollectionUpdaterManager;
import com.tryane.saas.connector.sharepoint.process.sitecollection.ISPSiteCollectionUpdaterProcess;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.connector.execution.ConnectorExecution;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.NetworkType;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class SPSiteCollectionUpdaterRunner extends AbstractSpringRunner {

	private static final Logger				LOGGER		= LoggerFactory.getLogger(SPSiteCollectionUpdaterRunner.class);

	private static final String				NETWORK_ID	= "s443673";

	@Autowired
	private INetworkPropertyManager			networkPropertyManager;

	@Autowired
	private INetworkManager					networkManager;

	@Autowired
	private IAppTokenManager				appTokenManager;

	@Autowired
	private IIDManager						idService;

	@Autowired
	private IConnectorContextInitialiser	connectorContextInitialiser;

	@Autowired
	private ISPSiteCollectionUpdaterProcess	siteCollectionUpdaterProcess;

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
			siteCollectionUpdaterProcess.updateAllSiteCollections(tenantId);
		} catch (InterruptedException e) {
			LOGGER.error("", e);
		}
	}

	public static void main(String[] args) {
		new SPSiteCollectionUpdaterRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

}
