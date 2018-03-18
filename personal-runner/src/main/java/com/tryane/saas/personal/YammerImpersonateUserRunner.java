package com.tryane.saas.personal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.tryane.saas.connector.yammer.api.exception.YammerConnectionException;
import com.tryane.saas.connector.yammer.api.exception.YammerHttpErrorException;
import com.tryane.saas.connector.yammer.newapi.IYammerOauthAPI;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class YammerImpersonateUserRunner {

	private static final Logger		LOGGER				= LoggerFactory.getLogger(YammerImpersonateUserRunner.class);

	private static final String		NETWORK_ID			= "6753900";

	private static final Long		USER_TO_IMPERSONATE	= 1628502167L;

	@Value("${yammer.app.clientid}")
	private String					tryaneAppId;

	@Autowired
	private INetworkManager			networkManager;

	@Autowired
	private INetworkPropertyManager	networkPropertyManager;

	@Autowired
	private IYammerOauthAPI			oauthApi;

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, PersonalDatabaseConfig.class);

			YammerImpersonateUserRunner runner = new YammerImpersonateUserRunner();
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);

			runner.execute();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	public void execute() {
		Network network = networkManager.getNetworkById(NETWORK_ID);
		ClientContextHolder.setNetwork(network);

		String tokenVa = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.YAMMER_TOKEN);

		try {
			String tokenUser = oauthApi.getImpersonnatedUserTokenByNetwork(tokenVa, USER_TO_IMPERSONATE.toString(), tryaneAppId, Long.valueOf(NETWORK_ID));
			LOGGER.info("token : {}", tokenUser);
		} catch (NumberFormatException | YammerConnectionException | YammerHttpErrorException e) {
			e.printStackTrace();
		}
	}
}
