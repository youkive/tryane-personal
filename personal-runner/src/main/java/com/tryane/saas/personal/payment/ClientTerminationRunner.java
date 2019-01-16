package com.tryane.saas.personal.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.payment.config.AppConfig;
import com.tryane.saas.payment.config.PaymentDatabaseConfig;
import com.tryane.saas.payment.statuschecker.terminated.ITerminatedManager;
import com.tryane.saas.payment.statuschecker.terminated.jsinjection.ISPUninstallInJectionService;
import com.tryane.saas.personal.AbstractSpringRunner;

public class ClientTerminationRunner extends AbstractSpringRunner {
	private static final Logger				LOGGER		= LoggerFactory.getLogger(ClientTerminationRunner.class);

	private static Long						CLIENT_ID	= 1L;

	private static String					NETWORK_ID	= "s1";

	@Autowired
	private ITerminatedManager				terminatedManager;

	@Autowired
	private ISPUninstallInJectionService	uninstallService;

	@Autowired
	private INetworkManager					networkManager;

	@Override
	protected void testImplementation() {
		ClientContextHolder.setClient(CLIENT_ID);
		Network network = networkManager.getNetworkById(NETWORK_ID);
		uninstallService.uninstallJsInjectionForNetwork(network);
		LOGGER.info("End Runner");
	}

	public static void main(String[] args) {
		new ClientTerminationRunner().runTest("dev", AppConfig.class, PaymentDatabaseConfig.class);
	}
}
