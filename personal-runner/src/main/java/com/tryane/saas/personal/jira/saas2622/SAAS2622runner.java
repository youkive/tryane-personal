package com.tryane.saas.personal.jira.saas2622;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.microsoft.aad.adal4j.AuthenticationResult;
import com.tryane.saas.connector.o365.utils.authentication.IO365Authenticator;
import com.tryane.saas.connector.o365.utils.exception.O365UserAuthenticationException;
import com.tryane.saas.connector.sharepoint.process.userupdate.ISPUserUpdateRightService;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.core.sp.site.ISPSiteManager;
import com.tryane.saas.core.sp.site.SPSite;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;
import com.tryane.saas.utils.hibernate.ICallBack;

public class SAAS2622runner {

	private static final Logger			LOGGER		= LoggerFactory.getLogger(SAAS2622runner.class);

	private final String				NETWORK_ID	= "";

	@Autowired
	private ISPUserUpdateRightService	spUserUpdateRightService;

	@Autowired
	private ISPSiteManager				webSiteManager;

	@Autowired
	private IO365Authenticator			authenticator;

	@Autowired
	private INetworkManager				networkManager;

	@Autowired
	private INetworkPropertyManager		networkPropertyManager;

	public static void main(String[] args) throws O365UserAuthenticationException {
		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, PersonalDatabaseConfig.class);

			SAAS2622runner runner = new SAAS2622runner();
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);

			runner.execute();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	public void execute() throws O365UserAuthenticationException {
		Network network = networkManager.getNetworkById(NETWORK_ID);
		ClientContextHolder.setNetwork(network);

		String tenant = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_TENANT);
		String mainCollectionUrl = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_MAINCOLLECTION_URL);

		LocalDate connectorStartDate = LocalDate.parse("2017-11-23");
		AuthenticationResult authent = authenticator.getAppAccessToken(mainCollectionUrl, tenant);
		webSiteManager.processAllValidSites(new SiteCallback(connectorStartDate, authent.getAccessToken()), connectorStartDate);
	}

	class SiteCallback implements ICallBack<SPSite> {

		private LocalDate	connectorStartDate;

		private String		token;

		public SiteCallback(LocalDate connectorStartDate, String token) {
			this.connectorStartDate = connectorStartDate;
			this.token = token;
		}

		@Override
		public void processObject(SPSite webSite) {
			spUserUpdateRightService.updateOwnersOfWebSite(webSite, token, connectorStartDate);
		}

	}
}
