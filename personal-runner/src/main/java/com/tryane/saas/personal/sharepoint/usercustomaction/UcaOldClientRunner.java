package com.tryane.saas.personal.sharepoint.usercustomaction;

import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.core.AbstractSpringRunner;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class UcaOldClientRunner extends AbstractSpringRunner {

	private static final String	NETWORK_ID	= "s1";

	@Autowired
	private INetworkManager		networkManager;

	public static void main(String[] args) {
		new UcaOldClientRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

	@Override
	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));
	}

}
