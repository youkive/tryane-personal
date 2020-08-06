package com.tryane.saas.personal.yammer;

import java.util.concurrent.atomic.AtomicLong;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.connector.yammer.api.exception.YammerConnectionException;
import com.tryane.saas.connector.yammer.api.exception.YammerHttpErrorException;
import com.tryane.saas.connector.yammer.common.model.user.YammerUser;
import com.tryane.saas.connector.yammer.newapi.IYammerUserAPI;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.collaborator.ICollaboratorManager;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class YammerCollabExternalAnalyser extends AbstractSpringRunner {

	private static final Logger		LOGGER		= LoggerFactory.getLogger(YammerCollabExternalAnalyser.class);

	private static final String		NETWORK_ID	= "425331";

	@Autowired
	private INetworkManager			networkManager;

	@Autowired
	private INetworkPropertyManager	networkPropertyManager;

	@Autowired
	private ICollaboratorManager	collaboratorManager;

	@Autowired
	private IYammerUserAPI			yammerUserApi;

	@Override
	protected void testImplementation() {
		Network network = networkManager.getNetworkById(NETWORK_ID);
		ClientContextHolder.setNetwork(network);

		String yammerToken = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.YAMMER_TOKEN);
		AtomicLong countExternalCollab = new AtomicLong(0);
		AtomicLong countCollab = new AtomicLong(0);
		AtomicLong countExternalCollabInDb = new AtomicLong(0);

		collaboratorManager.processAllValidCollaboratorsAndExtForClientAtDate(collaborator -> {
			countCollab.incrementAndGet();
			if (collaborator.isExternal()) {
				countExternalCollabInDb.incrementAndGet();
			}
			try {
				YammerUser yammerUser = yammerUserApi.getUserById(yammerToken, Long.parseLong(collaborator.getExternalId()));
				if (yammerUser.getGuest()) {
					countExternalCollab.incrementAndGet();
				}

			} catch (YammerConnectionException | YammerHttpErrorException | NumberFormatException e) {
				LOGGER.error("", e);
			}
		}, LocalDate.now());

		LOGGER.info("{} external collaborators in DB", countExternalCollabInDb.get());
		LOGGER.info("{} external Collab on {} collabs", countExternalCollab.get(), countCollab.get());
	}

	public static void main(String[] args) {
		new YammerCollabExternalAnalyser().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

}
