package com.tryane.saas.personal.sharepoint.usercustomaction;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.tryane.saas.connector.o365.utils.authentication.IO365Authenticator;
import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.connector.o365.utils.exception.O365UserAuthenticationException;
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

public class RemoveSpfxUcaRunner extends AbstractSpringRunner {

	private final static Logger						LOGGER		= LoggerFactory.getLogger(RemoveSpfxUcaRunner.class);

	private static final String						NETWORK_ID	= "s1";

	@Autowired
	private INetworkManager							networkManager;

	@Autowired
	private ISPSiteAPI								siteApi;

	@Autowired
	private ISPSiteCollectionManager				siteCollectionManager;

	@Autowired
	@Qualifier("spfxUcaRegistrationManager")
	private IUserCustomActionRegistrationManager	spfxUcaRegistrationManager;

	@Autowired
	private IO365Authenticator						authenticator;

	@Autowired
	private INetworkPropertyManager					networkPropertyManager;

	public static void main(String[] args) {
		new RemoveSpfxUcaRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

	@Override
	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));

		String tenantId = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_TENANT);
		String sharepointUrl = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_MAINCOLLECTION_URL);

		try {
			String token = authenticator.getAppAccessToken(sharepointUrl, tenantId).getAccessToken();

			siteCollectionManager.getAllSupervisedSiteCollectionsAtDate(LocalDate.now()).forEach(siteCollection -> {
				try {
					siteApi.getAllUserCustomActions(siteCollection.getUrl(), token).stream().filter(uca -> spfxUcaRegistrationManager.isUserCustomActionRegisteredByTryane(uca)).forEach(uca -> {
						try {
							siteApi.deleteUserCustomAction(siteCollection.getUrl(), token, uca.getId());
						} catch (O365ConnectionException | O365HttpErrorException e) {
							LOGGER.error("", e);
						}
					});
				} catch (O365ConnectionException | O365HttpErrorException e) {
					LOGGER.error("", e);
				}
			});
		} catch (O365UserAuthenticationException e) {
			LOGGER.warn("{}", e);
		}
	}

}
