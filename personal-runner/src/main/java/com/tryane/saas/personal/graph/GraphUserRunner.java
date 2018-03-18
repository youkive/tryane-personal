package com.tryane.saas.personal.graph;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.microsoft.aad.adal4j.AuthenticationResult;
import com.tryane.saas.connector.azure.utils.api.AzureADUserApi;
import com.tryane.saas.connector.azure.utils.model.ADUser;
import com.tryane.saas.connector.o365.utils.IO365Resources;
import com.tryane.saas.connector.o365.utils.authentication.IO365Authenticator;
import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.connector.o365.utils.exception.O365UserAuthenticationException;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.client.ClientPropertyNames;
import com.tryane.saas.core.client.properties.IClientPropertyManager;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.core.network.NetworkType;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkSPSubType;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;
import com.tryane.saas.utils.hibernate.ICallBack;

public class GraphUserRunner {

	private static final Logger		LOGGER		= LoggerFactory.getLogger(GraphUserRunner.class);

	private final String			NETWORK_ID	= "6557";

	@Autowired
	private IO365Authenticator		authenticator;

	@Autowired
	private INetworkManager			networkManager;

	@Autowired
	private INetworkPropertyManager	networkPropertyManager;

	@Autowired
	private IClientPropertyManager	clientPropertyManager;

	public static void main(String[] args) throws O365UserAuthenticationException, O365ConnectionException, O365HttpErrorException, IOException {
		System.setProperty("spring.profiles.active", "dev");
		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, PersonalDatabaseConfig.class);

			GraphUserRunner runner = new GraphUserRunner();
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);

			runner.execute();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	public void execute() throws O365UserAuthenticationException, O365ConnectionException, O365HttpErrorException, IOException {
		Network network = networkManager.getNetworkById(NETWORK_ID);
		ClientContextHolder.setNetwork(network);

		String tenant = clientPropertyManager.getClientPropertyValue(ClientContextHolder.getClientId(), ClientPropertyNames.O365_TENANT_ID);
		AuthenticationResult authent = authenticator.getDelegateAuthenticatorFor(NetworkType.SHAREPOINT, NetworkSPSubType.OFFICE_365).getAppAccessToken(IO365Resources.AZURE_AD_RESOURCE, tenant);

		ICallBack<ADUser> callback = getCallback();
		AzureADUserApi.processAllUsers(tenant, authent.getAccessToken(), callback);
	}

	private ICallBack<ADUser> getCallback() {
		return new ICallBack<ADUser>() {

			@Override
			public void processObject(ADUser graphUser) {
				String email = graphUser.getMail();
				if (email == null || email.isEmpty()) {
					return;
				}
				if (email.contains("zenchenko") || email.contains("gutauskiene") || email.contains("pasic") || email.contains("prt.ext@velux.com") || email.contains("salihbegovic")) {
					LOGGER.info("graph id : {}", graphUser.getObjectId());
					LOGGER.info("graph Email : {}", graphUser.getMail());
					LOGGER.info("graph account Enabled :: {}", graphUser.getAccountEnabled());
					LOGGER.info("graph UPN : ", graphUser.getUserPrincipalName());
				}
			}
		};
	}
}
