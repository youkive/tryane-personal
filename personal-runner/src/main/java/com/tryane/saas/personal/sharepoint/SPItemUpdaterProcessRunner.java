package com.tryane.saas.personal.sharepoint;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.connector.common.manager.context.IConnectorContextInitialiser;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.connector.execution.ConnectorExecution;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class SPItemUpdaterProcessRunner extends AbstractSpringRunner {

	private static final String				NETWORK_ID	= "s443673";

	@Autowired
	private INetworkManager					networkManager;

	@Autowired
	private INetworkPropertyManager			networkPropertyManager;

	@Autowired
	private IConnectorContextInitialiser	connectorContextInitialiser;

	@Override
	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));
		ConnectorExecution connectorExceution = new ConnectorExecution();
		connectorContextInitialiser.initConnectorContext(LocalDate.now(), LocalDate.now(), connectorExceution);

	}

	public static void main(String[] args) {
		new SPItemUpdaterProcessRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}
}
