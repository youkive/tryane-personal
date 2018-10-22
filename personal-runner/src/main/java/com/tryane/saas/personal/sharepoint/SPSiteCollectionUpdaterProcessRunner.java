package com.tryane.saas.personal.sharepoint;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.connector.common.manager.collaboratorid.IIDManager;
import com.tryane.saas.connector.o365.utils.token.IAppTokenManager;
import com.tryane.saas.connector.sharepoint.process.sitecollection.ISPSiteCollectionUpdaterProcess;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.connector.context.ConnectorContext;
import com.tryane.saas.core.connector.execution.ConnectorExecution;
import com.tryane.saas.core.connector.stats.ConnectorStats;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class SPSiteCollectionUpdaterProcessRunner extends AbstractSpringRunner {

	private static final Logger				LOGGER		= LoggerFactory.getLogger(SPSiteCollectionUpdaterProcessRunner.class);

	private final String					NETWORK_ID	= "s443708";

	private final LocalDate					START_DATE	= LocalDate.now().minusDays(1);

	@Autowired
	private INetworkPropertyManager			networkPropertyManager;

	@Autowired
	private INetworkManager					networkManager;

	@Autowired
	private IAppTokenManager				appTokenManager;

	@Autowired
	private IIDManager						idManager;

	@Autowired
	private ISPSiteCollectionUpdaterProcess	siteCollectionUpdaterProcess;

	@Override
	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));

		String tenantId = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_TENANT);
		appTokenManager.initForTenant(tenantId);

		idManager.initForCurrentNetwork();
		ConnectorExecution connectorExecution = new ConnectorExecution();
		ConnectorStats connectorStats = new ConnectorStats();
		ConnectorContext.init(connectorExecution, START_DATE, START_DATE, false, connectorStats);
		try {
			siteCollectionUpdaterProcess.updateAllSiteCollections(tenantId);
		} catch (InterruptedException e) {
			LOGGER.error("", e);
		}
	}

	public static void main(String[] args) {
		new SPSiteCollectionUpdaterProcessRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}
}
