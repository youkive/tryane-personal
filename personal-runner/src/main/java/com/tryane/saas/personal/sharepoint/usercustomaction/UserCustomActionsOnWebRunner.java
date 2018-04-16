package com.tryane.saas.personal.sharepoint.usercustomaction;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.microsoft.aad.adal4j.AuthenticationResult;
import com.tryane.saas.connector.o365.utils.authentication.IO365Authenticator;
import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.connector.o365.utils.exception.O365UserAuthenticationException;
import com.tryane.saas.connector.sharepoint.utils.api.ISPWebAPI;
import com.tryane.saas.connector.sharepoint.utils.model.SharepointSPWeb;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;
import com.tryane.saas.personal.extension.shrarepoint.api.ISPWebApiExtension;
import com.tryane.saas.personal.extension.shrarepoint.api.SPWebApiExtension;
import com.tryane.saas.utils.hibernate.ICallBack;

public class UserCustomActionsOnWebRunner {

	private static final Logger		LOGGER							= LoggerFactory.getLogger(UserCustomActionsOnWebRunner.class);

	private static final String		NETWORK_ID						= "s2964";

	private static final String		SITE_COLLECTION_URL_TO_SURVEY	= "https://roquettegroup.sharepoint.com/teams/collab";

	@Autowired
	private INetworkManager			networkManager;

	@Autowired
	private INetworkPropertyManager	networkPropertyManager;

	@Autowired
	private IO365Authenticator		o365Authenticator;

	@Autowired
	private ISPWebAPI				spWebApi;

	private ISPWebApiExtension		spWebApiExtension				= new SPWebApiExtension();

	public static void main(String[] args) throws O365UserAuthenticationException, O365ConnectionException, O365HttpErrorException {
		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, PersonalDatabaseConfig.class);
			UserCustomActionsOnWebRunner runner = new UserCustomActionsOnWebRunner();
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);
			ctx.getAutowireCapableBeanFactory().autowireBean(runner.spWebApiExtension);

			runner.execute();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	public void execute() throws O365UserAuthenticationException, O365ConnectionException, O365HttpErrorException {
		Network network = networkManager.getNetworkById(NETWORK_ID);
		ClientContextHolder.setNetwork(network);

		String tenantId = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_TENANT);
		String resourceUrl = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_MAINCOLLECTION_URL);
		AuthenticationResult authent = o365Authenticator.getDelegateAuthenticator().getAppAccessToken(resourceUrl, tenantId);

		AtomicInteger nbWebSite = new AtomicInteger(0);
		spWebApi.processAllSubWebSites(SITE_COLLECTION_URL_TO_SURVEY, authent.getAccessToken(), 3, new ICallBack<SharepointSPWeb>() {

			@Override
			public void processObject(SharepointSPWeb webSite) {
				nbWebSite.incrementAndGet();
				AtomicBoolean find = new AtomicBoolean(false);
				try {
					spWebApiExtension.getAllUserCustomActions(webSite.getUrl(), authent.getAccessToken()).forEach(userCustomAction -> {
						if (userCustomAction.getScriptBlock() != null && userCustomAction.getScriptBlock().contains("tryane")) {
							find.set(true);
						}
					});
				} catch (O365ConnectionException | O365HttpErrorException e) {
					e.printStackTrace();
				}

				if (find.get()) {
					LOGGER.info("possible for {}", webSite.getUrl());
				}
			}
		});

		LOGGER.info("nb Web sites analysed : {}", nbWebSite.get());
	}
}
