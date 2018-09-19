package com.tryane.saas.personal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.core.network.feature.INetworkFeatureManager;
import com.tryane.saas.core.network.feature.NetworkFeature;
import com.tryane.saas.core.network.feature.NetworkFeatureId;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class NetworkFeatureCreationRunner extends AbstractSpringRunner {

	private static final Logger				LOGGER		= LoggerFactory.getLogger(NetworkFeatureCreationRunner.class);

	private static final String				NETWORK_ID	= "s1";

	private static final NetworkFeatureId	FEATURE_ID	= NetworkFeatureId.SP_O365_MANAGMENT_ACTI_API_SPFX_EXTENSION;

	@Autowired
	private INetworkFeatureManager			networkFeatureManager;

	@Autowired
	private INetworkManager					networkManager;

	public static void main(String[] args) {
		new NetworkFeatureCreationRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

	@Override
	protected void testImplementation() {
		Network currentNetwork = networkManager.getNetworkById(NETWORK_ID);
		if (!networkFeatureManager.hasFeature(NETWORK_ID, FEATURE_ID)) {
			networkFeatureManager.createNetworkFeature(new NetworkFeature(currentNetwork.getClientId(), currentNetwork.getNetworkId(), FEATURE_ID));
		} else {
			LOGGER.info("feature already exists");
		}
	}

}
