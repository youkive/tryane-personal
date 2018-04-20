package com.tryane.saas.personal.sharepoint.usercustomaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.connector.o365.utils.exception.O365UserAuthenticationException;
import com.tryane.saas.connector.o365.utils.token.IAppTokenManager;
import com.tryane.saas.connector.sharepoint.sitecollections.usercutomactions.registration.IUserCustomActionRegistrationManager;
import com.tryane.saas.connector.sharepoint.utils.api.ISPSiteAPI;
import com.tryane.saas.core.AbstractSpringRunner;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.core.sp.sitecol.ISPSiteCollectionManager;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class RemoveJsUcaRunner extends AbstractSpringRunner {

	private static final Logger						LOGGER		= LoggerFactory.getLogger(RemoveJsUcaRunner.class);

	private static final String						NETWORK_ID	= "s1";

	@Autowired
	private INetworkManager							networkManager;

	@Autowired
	private INetworkPropertyManager					networkPropertyManager;

	@Autowired
	private ISPSiteCollectionManager				siteCollectionManager;

	@Autowired
	private ISPSiteAPI								siteApi;

	@Autowired
	@Qualifier("jsUcaRegistrationManager")
	private IUserCustomActionRegistrationManager	jsUcaRegistrationManager;

	@Autowired
	private IAppTokenManager						appTokenManager;

	public static void main(String[] args) {
		new RemoveJsUcaRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

	@Override
	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));
		String tenantId = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_TENANT);

		appTokenManager.initForTenant(tenantId);
		siteCollectionManager.getAllSiteCollectionsNotDeleted().forEach(siteCollection -> {
			try {
				String token = appTokenManager.geAppTokenGenerator(siteCollection.getRootUrl(), tenantId).getToken();
				siteApi.getAllUserCustomActions(siteCollection.getUrl(), token).stream().filter(uca -> jsUcaRegistrationManager.isUserCustomActionRegisteredByTryane(uca)).forEach(uca -> {
					try {
						siteApi.deleteUserCustomAction(siteCollection.getUrl(), token, uca.getId());
					} catch (O365ConnectionException | O365HttpErrorException e) {
						LOGGER.error("", e);
					}
				});
			} catch (O365UserAuthenticationException | O365ConnectionException | O365HttpErrorException e) {
				LOGGER.error("", e);
			}
		});
	}

}
