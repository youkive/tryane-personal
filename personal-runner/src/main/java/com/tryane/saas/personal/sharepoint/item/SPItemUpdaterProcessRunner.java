package com.tryane.saas.personal.sharepoint.item;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.connector.common.manager.collaboratorid.IIDManager;
import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.connector.o365.utils.exception.O365UserAuthenticationException;
import com.tryane.saas.connector.o365.utils.token.IAppTokenManager;
import com.tryane.saas.connector.sharepoint.process.item.ISPItemUpdaterProcess;
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

public class SPItemUpdaterProcessRunner extends AbstractSpringRunner {

	private static final String		NETWORK_ID	= "s1";

	private static final Logger		LOGGER		= LoggerFactory.getLogger(SPItemUpdaterProcessRunner.class);

	@Autowired
	private INetworkManager			networkManager;

	@Autowired
	private INetworkPropertyManager	networkPropertyManager;

	@Autowired
	private ISPItemUpdaterProcess	spItemUpdaterProcess;

	@Autowired
	private IAppTokenManager		appTokenManager;

	@Autowired
	private IIDManager				idManager;

	@Override
	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));
		String tenantId = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_TENANT);

		ConnectorContext.init(new ConnectorExecution(), LocalDate.now(), LocalDate.now(), LocalDate.now(), false, new ConnectorStats());
		appTokenManager.initForTenant(tenantId);
		idManager.initForCurrentNetwork();

		try {
			spItemUpdaterProcess.updateItems(tenantId);
		} catch (O365UserAuthenticationException | O365ConnectionException | O365HttpErrorException | InterruptedException e) {
			LOGGER.error("SPItemUpdaterProcessRunner Unexpected error", e);
		} finally {
			appTokenManager.clearForTenant(tenantId);
			idManager.clearForCurrentNetwork();
		}
	}

	public static void main(String[] args) {
		new SPItemUpdaterProcessRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

}
