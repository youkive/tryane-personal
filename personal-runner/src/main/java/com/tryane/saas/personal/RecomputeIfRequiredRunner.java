package com.tryane.saas.personal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.db.updater.config.DatabaseConfig;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.recompute.manager.IRecomputeManager;

public class RecomputeIfRequiredRunner extends AbstractSpringRunner {

	private static final Logger	LOGGER		= LoggerFactory.getLogger(RecomputeIfRequiredRunner.class);

	private static final String	NETWORK_ID	= "s443708";

	@Autowired
	private INetworkManager		networkManager;

	@Autowired
	private IRecomputeManager	recomputeManager;

	@Override
	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));
		LOGGER.info("RECOMPUTE");
		recomputeManager.recomputeIfRequired();
	}

	public static void main(String[] args) {
		new RecomputeIfRequiredRunner().runTest("dev", PersonalAppConfig.class, DatabaseConfig.class);
	}

}
