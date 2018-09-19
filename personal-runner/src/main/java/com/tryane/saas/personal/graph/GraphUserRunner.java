package com.tryane.saas.personal.graph;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.tryane.saas.connector.graph.api.IGraphUserApi;
import com.tryane.saas.connector.graph.model.GraphUser;
import com.tryane.saas.connector.o365.utils.IO365Resources;
import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.connector.o365.utils.exception.O365UserAuthenticationException;
import com.tryane.saas.connector.o365.utils.token.IAppTokenManager;
import com.tryane.saas.connector.o365.utils.token.IO365TokenSupplier;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.client.ClientPropertyNames;
import com.tryane.saas.core.client.properties.IClientPropertyManager;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;
import com.tryane.saas.utils.hibernate.ICallBack;

public class GraphUserRunner {

	private static final Logger		LOGGER		= LoggerFactory.getLogger(GraphUserRunner.class);

	private final String			NETWORK_ID	= "s1";

	@Autowired
	private INetworkManager			networkManager;

	@Autowired
	private IClientPropertyManager	clientPropertyManager;

	@Autowired
	private IGraphUserApi			graphUserApi;

	@Autowired
	private IAppTokenManager		appTokenManager;

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

		try {
			appTokenManager.initForTenant(tenant);

			GraphUserCallback callback = new GraphUserCallback();
			graphUserApi.processAllUsers(new IO365TokenSupplier() {

				@Override
				public String getToken() throws O365UserAuthenticationException {
					return appTokenManager.geAppTokenGenerator(IO365Resources.GRAPH_RESOURCE, tenant).getToken();
				}

				@Override
				public String getFreshToken() throws O365UserAuthenticationException {
					return appTokenManager.geAppTokenGenerator(IO365Resources.GRAPH_RESOURCE, tenant).getFreshToken();
				}
			}, callback);
			callback.finish();
		} finally {
			appTokenManager.clearForTenant(tenant);
		}
	}

	class GraphUserCallback implements ICallBack<GraphUser> {

		private AtomicLong count;

		public GraphUserCallback() {
			count = new AtomicLong(0);
		}

		@Override
		public void processObject(GraphUser graphUser) {
			count.incrementAndGet();
			displayCount();
		}

		private void displayCount() {
			if (count.get() % 100 == 0) {
				LOGGER.info("on est à {} users processed", count.get());
			}
		}

		private void displayUser(GraphUser graphUser) {
			LOGGER.info("graph id : {}", graphUser.getId());
			LOGGER.info("graph Email : {}", graphUser.getMail());
			LOGGER.info("graph account Enabled :: {}", graphUser.getAccountEnabled());
			LOGGER.info("graph UPN : ", graphUser.getUserPrincipalName());
		}

		private void finish() {
			LOGGER.info("TOTAL : {} users processed", count.get());
		}
	}
}
