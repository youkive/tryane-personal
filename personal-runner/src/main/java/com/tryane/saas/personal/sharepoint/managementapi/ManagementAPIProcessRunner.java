package com.tryane.saas.personal.sharepoint.managementapi;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.connector.common.manager.collaboratorid.IIDManager;
import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.connector.o365.utils.exception.O365UserAuthenticationException;
import com.tryane.saas.connector.o365.utils.token.IAppTokenManager;
import com.tryane.saas.connector.sharepoint.process.o365managmentapi.IO365ManagementAPIReadEventsProcess;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.connector.context.ConnectorContext;
import com.tryane.saas.core.connector.execution.ConnectorEventAvailable.EventSource;
import com.tryane.saas.core.connector.execution.ConnectorExecution;
import com.tryane.saas.core.connector.stats.ConnectorStats;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class ManagementAPIProcessRunner extends AbstractSpringRunner {

	private static final Logger					LOGGER	= LoggerFactory.getLogger(ManagementAPIProcessRunner.class);

	@Autowired
	private IO365ManagementAPIReadEventsProcess	managmentAPIProcess;

	@Autowired
	private INetworkManager						networkManager;

	@Autowired
	private INetworkPropertyManager				networkPropertyManager;

	@Autowired
	private IAppTokenManager					appTokenManager;

	@Autowired
	private IIDManager							idManager;

	@Override
	protected void testImplementation() {
		Network currentNetwork = networkManager.getNetworkById("s443861");
		ClientContextHolder.setNetwork(currentNetwork);

		Map<EventSource, LocalDate> connectorStartDatesBySource = new HashMap<>();
		connectorStartDatesBySource.put(EventSource.DEFAULT, LocalDate.now().minusDays(1));
		Map<EventSource, LocalDate> connectorEndDatesBySource = new HashMap<>();
		connectorEndDatesBySource.put(EventSource.DEFAULT, LocalDate.now().minusDays(1));
		ConnectorExecution connectorExecution = new ConnectorExecution();
		Map<EventSource, LocalDate> connectorInitDates = new HashMap<>();
		Boolean isFirstRun = false;
		ConnectorStats connectorStats = new ConnectorStats();
		ConnectorContext.init(connectorExecution, connectorStartDatesBySource, connectorEndDatesBySource, connectorInitDates, isFirstRun, connectorStats);

		String tenantId = networkPropertyManager.getNetworkPropertyValue(currentNetwork.getNetworkId(), NetworkPropertyNames.SHAREPOINT_TENANT);
		appTokenManager.initForTenant(tenantId);

		idManager.initForCurrentNetwork();

		try {
			managmentAPIProcess.processEventsInManagmentApi();
		} catch (O365ConnectionException | O365HttpErrorException | O365UserAuthenticationException e) {
			LOGGER.error("{}", e);
		}
	}

	public static void main(String[] args) {
		new ManagementAPIProcessRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

}
