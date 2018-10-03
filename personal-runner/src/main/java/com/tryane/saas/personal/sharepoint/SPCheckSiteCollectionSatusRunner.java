package com.tryane.saas.personal.sharepoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.connector.o365.utils.exception.O365UserAuthenticationException;
import com.tryane.saas.connector.o365.utils.token.IAppTokenManager;
import com.tryane.saas.connector.sharepoint.utils.api.ISPSiteAPI;
import com.tryane.saas.connector.sharepoint.utils.model.SharepointSPSite;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.core.sp.sitecol.ISPSiteCollectionManager;
import com.tryane.saas.core.sp.sitecol.SpSiteCollectionConnectionStatus;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class SPCheckSiteCollectionSatusRunner extends AbstractSpringRunner {

	private static final Logger			LOGGER		= LoggerFactory.getLogger(SPCheckSiteCollectionSatusRunner.class);

	private static final String			NETWORK_ID	= "s443708";

	@Autowired
	private INetworkManager				networkManager;

	@Autowired
	private INetworkPropertyManager		networkPropertyManager;

	@Autowired
	private ISPSiteCollectionManager	siteCollectionManager;

	@Autowired
	private IAppTokenManager			tokenManager;

	@Autowired
	private ISPSiteAPI					siteApi;

	public static void main(String[] args) {
		new SPCheckSiteCollectionSatusRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

	@Override
	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));

		String tenant = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_TENANT);
		String mainCollectionUrl = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_MAINCOLLECTION_URL);
		tokenManager.initForTenant(tenant);

		siteCollectionManager.getAllSiteCollections().stream().filter(siteCollection -> {
			return siteCollection.getIsSupervised() && !siteCollection.getConnectionStatus().equals(SpSiteCollectionConnectionStatus.AVAILABLE);
		}).forEach(siteCollection -> {
			try {
				String token = tokenManager.geAppTokenGenerator(mainCollectionUrl, tenant).getToken();
				SharepointSPSite siteFromSp = siteApi.getSite(siteCollection.getUrl(), token);
				LOGGER.info("{}", siteCollection.getUrl());
			} catch (O365UserAuthenticationException e) {
				LOGGER.error("", e);
			} catch (O365ConnectionException e) {
				LOGGER.error("", e);
			} catch (O365HttpErrorException e) {
				LOGGER.error("", e);
			}
		});
	}

}
