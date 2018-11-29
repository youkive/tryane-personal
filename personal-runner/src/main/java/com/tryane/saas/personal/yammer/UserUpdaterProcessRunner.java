package com.tryane.saas.personal.yammer;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.connector.yammer.process.userupdate.IUserUpdaterProcess;
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

public class UserUpdaterProcessRunner extends AbstractSpringRunner {
	private static final String NETWORK_ID = "41556";
	
	@Autowired
	private INetworkManager networkManager;
	
	@Autowired
	private INetworkPropertyManager networkPropertyManager;
	
	@Autowired
	private IUserUpdaterProcess userUpdaterProcess;
	
	@Override
	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));
		String token = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.YAMMER_TOKEN);
		
		
		ConnectorContext.init(new ConnectorExecution(), LocalDate.now(), LocalDate.now(), false, new ConnectorStats());
		userUpdaterProcess.updateUsers(token);
	}
	
	public static void main(String[] args) {
		new UserUpdaterProcessRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}
}
