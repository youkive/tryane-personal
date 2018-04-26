package com.tryane.saas.personal.sharepoint.spfxaddon;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.connector.o365.utils.authentication.IO365Authenticator;
import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.connector.o365.utils.exception.O365UserAuthenticationException;
import com.tryane.saas.connector.sharepoint.sitecollections.usercutomactions.remove.ISPInjectionRemoveManager;
import com.tryane.saas.connector.sharepoint.utils.api.ISPALMAPI;
import com.tryane.saas.core.AbstractSpringRunner;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.core.network.NetworkType;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.core.sp.sitecol.ISPSiteCollectionManager;
import com.tryane.saas.core.sp.sitecol.SPSiteCollection;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class RemoveSpfxAddonRunner extends AbstractSpringRunner {

	private static final Logger			LOGGER		= LoggerFactory.getLogger(RemoveSpfxAddonRunner.class);

	private final String				NETWORK_ID	= "s1";

	@Autowired
	private INetworkManager				networkManager;

	@Autowired
	private INetworkPropertyManager		networkPropertyManager;

	@Autowired
	private ISPSiteCollectionManager	siteCollectionManager;

	@Autowired
	private IO365Authenticator			o365authenticator;

	@Autowired
	private ISPInjectionRemoveManager	injectionRemoveManager;

	@Autowired
	private ISPALMAPI					spALMAPI;

	public static void main(String[] args) {
		new RemoveSpfxAddonRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

	@Override
	protected void testImplementation() {
		Network network = networkManager.getNetworkById(NETWORK_ID);
		if (!NetworkType.SHAREPOINT.equals(network.getType())) {
			return;
		}

		ClientContextHolder.setNetwork(network);
		LOGGER.info("Start uninstallation Addon injection of network {}", ClientContextHolder.getNetworkId());

		String tenantId = networkPropertyManager.getNetworkPropertyValue(ClientContextHolder.getNetworkId(), NetworkPropertyNames.SHAREPOINT_TENANT);

		List<SPSiteCollection> siteCollectionsMonitored = siteCollectionManager.getAllMonitoredSiteCollections();
		for (SPSiteCollection siteCollection : siteCollectionsMonitored) {
			String token;
			try {
				String networkSubType = ClientContextHolder.getNetwork().getPropertyInPropsAsString(NetworkPropertyNames.O365_PRODUCT_SUB_TYPE);
				token = o365authenticator.getDelegateAuthenticatorFor(ClientContextHolder.getNetworkType(), networkSubType).getAppAccessToken(siteCollection.getRootUrl(), tenantId).getAccessToken();
				injectionRemoveManager.removeAllUserCustomActionRegisteredByTryaneOnSiteCollection(siteCollection.getId(), token);
				spALMAPI.retractSolutionPackageInTenantAppCatalog(siteCollection.getUrl(), token);
				spALMAPI.removeSolutionPackageInTenantAppCatalog(siteCollection.getUrl(), token);
			} catch (O365UserAuthenticationException | O365ConnectionException | O365HttpErrorException e) {
				LOGGER.error("", e);
			}
		}
		LOGGER.info("End uninstallation Addon injection of network {}", ClientContextHolder.getNetworkId());

	}

}
