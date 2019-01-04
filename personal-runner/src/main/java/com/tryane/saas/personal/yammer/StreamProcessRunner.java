package com.tryane.saas.personal.yammer;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.connector.yammer.process.bycollab.stream.IStreamProcess;
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

public class StreamProcessRunner extends AbstractSpringRunner {

	private static final Logger		LOGGER		= LoggerFactory.getLogger(StreamProcessRunner.class);

	private static final String		NETWORK_ID	= "2267546";

	@Autowired
	private INetworkManager			networkManager;

	@Autowired
	private INetworkPropertyManager	networkPropertyManager;

	@Autowired
	private IStreamProcess			streamProcess;

	public static void main(String[] args) {
		new StreamProcessRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

	@Override
	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));

		String token = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.YAMMER_TOKEN);

		ConnectorExecution connExecution = new ConnectorExecution();
		ConnectorStats connectorStats = new ConnectorStats();
		ConnectorContext.init(connExecution, LocalDate.parse("2018-12-01"), LocalDate.now(), null, false, connectorStats);
		streamProcess.generateEventsFromCollaboratorsStreams(token);
	}

}
