package com.tryane.saas.personal;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.core.client.lowcost.ILowCostManager;
import com.tryane.saas.core.client.lowcost.LowCostConfig;
import com.tryane.saas.core.client.lowcost.LowCostModule;
import com.tryane.saas.core.network.NetworkType;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class LowCostConfigRunner extends AbstractSpringRunner {

	private static final Long	CLIENT_ID	= 1L;

	@Autowired
	private ILowCostManager		lowCostManager;

	@Override
	protected void testImplementation() {
		LowCostConfig lowCostConfig = new LowCostConfig();
		lowCostConfig.setNbUsersRestriction(20);
		LowCostModule lowCostModule = new LowCostModule();
		lowCostModule.setContentsRestriction(true);
		HashMap<NetworkType, LowCostModule> mapLowCostConfig = new HashMap<>();
		mapLowCostConfig.put(NetworkType.SHAREPOINT, lowCostModule);
		lowCostConfig.setRestictionByModule(mapLowCostConfig);
		lowCostManager.updateLowCostConfig(CLIENT_ID, lowCostConfig);
	}

	public static void main(String[] args) {
		new LowCostConfigRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

}
