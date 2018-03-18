package com.tryane.saas.personal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.connector.configuration.ConnectorConfiguration;
import com.tryane.saas.core.connector.configuration.ConnectorConfiguration.CsvCollabIdKeyType;
import com.tryane.saas.core.connector.configuration.IConnectorConfigurationManager;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.properties.CollaboratorPropertiesSource;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;
import com.tryane.saas.utils.jackson.JacksonUtils;

public class NetworkPropertyRunner {

	public static final String				NETWORK_ID		= "1897800";

	public static final String				PROPERTY_NAME	= NetworkPropertyNames.YAMMER_PRIVATE_GROUP_ACCESS_ADMIN_ONLY;

	public final String						NEW_VALUE		= Boolean.TRUE.toString();

	public static final Logger				LOGGER			= LoggerFactory.getLogger(NetworkPropertyRunner.class);

	@Autowired
	private IConnectorConfigurationManager	connectorConfigurationManager;

	@Autowired
	private INetworkPropertyManager			networkPropertyManager;

	@Autowired
	private INetworkManager					networkManager;

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "cache-redis");
		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, PersonalDatabaseConfig.class);
			NetworkPropertyRunner runner = new NetworkPropertyRunner();
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);
			runner.execute();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	private void execute() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));
		setNetworkProperty();
		//displayNetworkProperty();
		//setConnectorConfig();
	}

	private void displayNetworkProperty() {
		String propertyValue = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, PROPERTY_NAME);
		LOGGER.info(propertyValue);
	}

	private void setNetworkProperty() {
		networkPropertyManager.setNetworkPropertyValue(NETWORK_ID, PROPERTY_NAME, NEW_VALUE);
	}

	private void setConnectorConfig() {
		ConnectorConfiguration connectorConfiguration = new ConnectorConfiguration();
		connectorConfiguration.setCsvSourceEnabledforCollaboratorModel(true);
		connectorConfiguration.getSources().add(CollaboratorPropertiesSource.CSV);
		connectorConfiguration.setCsvDisplayNameHeaderName("name");
		connectorConfiguration.setCsvEmailHeaderName("email");
		connectorConfiguration.setCsvCollabIdKeyHeaderName("id");
		connectorConfiguration.setCsvCollabIdKeyType(CsvCollabIdKeyType.SHAREPOINT_ID);
		//connectorConfigurationManager.setConnectorConfiguration(connectorConfiguration);
		try {
			LOGGER.info(JacksonUtils.createJsonResult(connectorConfiguration));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
