package com.tryane.saas.personal.sharepoint;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.aad.adal4j.AuthenticationResult;
import com.tryane.saas.connector.graph.api.IGraphGroupAPI;
import com.tryane.saas.connector.graph.model.teams.GraphGroup;
import com.tryane.saas.connector.o365.utils.IO365Resources;
import com.tryane.saas.connector.o365.utils.authentication.IO365Authenticator;
import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.connector.o365.utils.exception.O365UserAuthenticationException;
import com.tryane.saas.connector.o365.utils.token.IO365TokenSupplier;
import com.tryane.saas.connector.sharepoint.utils.api.ISPWebAPI;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.core.sp.sitecol.ISPSiteCollectionManager;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class SearchSecurityGroupOwnerRunner extends AbstractSpringRunner {

	private static final Logger			LOGGER		= LoggerFactory.getLogger(SearchSecurityGroupOwnerRunner.class);

	private static final String			NETWORK_ID	= "s443708";

	@Autowired
	private INetworkManager				networkManager;

	@Autowired
	private ISPWebAPI					webApi;

	@Autowired
	private ISPSiteCollectionManager	siteCollectionManager;

	@Autowired
	private IO365Authenticator			o365Authenticator;

	@Autowired
	private INetworkPropertyManager		networkPropertyManager;

	@Autowired
	private IGraphGroupAPI				graphGroupApi;

	@Override
	protected void testImplementation() {
		LOGGER.info("START RUNNER");
		AtomicLong compteur = new AtomicLong(0);
		AtomicLong compteurError = new AtomicLong(0);

		Network network = networkManager.getNetworkById(NETWORK_ID);
		ClientContextHolder.setNetwork(network);

		String spRootUrl = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_MAINCOLLECTION_URL);
		String tenantId = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_TENANT);

		try {
			AuthenticationResult spAuthent = o365Authenticator.getDelegateAuthenticator().getAppAccessToken(spRootUrl, tenantId);
			AuthenticationResult graphAuthent = o365Authenticator.getDelegateAuthenticator().getAppAccessToken(IO365Resources.GRAPH_RESOURCE, tenantId);

			siteCollectionManager.getAllSiteCollectionsNotDeleted().forEach(siteCollection -> {
				try {
					webApi.getAllUsersOfSiteCollection(siteCollection.getUrl(), spAuthent.getAccessToken()).forEach(user -> {
						if (user.getLoginName().contains("federateddirectoryclaimprovider") && user.getLoginName().endsWith("_o")) {
							String result = user.getLoginName().replaceFirst("c:0o.c\\|federateddirectoryclaimprovider\\|", "");
							result = result.replaceFirst("_o", "");
							try {
								GraphGroup finalGroup = graphGroupApi.getGroupWithId(new IO365TokenSupplier() {

									@Override
									public String getToken() throws O365UserAuthenticationException {
										return graphAuthent.getAccessToken();
									}
								}, result);
								compteur.getAndIncrement();
							} catch (O365ConnectionException | O365HttpErrorException | O365UserAuthenticationException e) {
								LOGGER.error("", e);
								compteurError.getAndIncrement();
							}
							LOGGER.info("{}", result);
						}
					});
				} catch (O365ConnectionException | O365HttpErrorException e) {
					LOGGER.error("", e);
				}
			});
		} catch (O365UserAuthenticationException e) {
			LOGGER.error("", e);
		}

		LOGGER.info("find {} group owner", compteur.get());
		LOGGER.info("cannot find {} group references", compteurError.get());
		LOGGER.info("END RUNNER");
	}

	public static void main(String[] args) {
		new SearchSecurityGroupOwnerRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

}
