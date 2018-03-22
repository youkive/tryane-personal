package com.tryane.saas.personal.sharepoint;

import java.io.IOException;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.tryane.saas.connector.collaborators.properties.ICollaboratorPropertiesUpdateServiceDelegate;
import com.tryane.saas.connector.o365.utils.token.IAppTokenManager;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.collaborator.update.CollaboratorImportResult;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;
import com.tryane.saas.utils.exception.TryaneException;

public class SPCollaboratorProfileUpdaterPerfRunner {

	private static final Logger								LOGGER		= LoggerFactory.getLogger(SPCollaboratorProfileUpdaterPerfRunner.class);

	private final String									NETWORK_ID	= "s1";

	@Autowired
	@Qualifier("spFromUserProfileCollaboratorPropertiesUpdateServiceImpl")
	private ICollaboratorPropertiesUpdateServiceDelegate	spCollabProfilePropertiesUpdater;

	@Autowired
	private INetworkManager									networkManager;

	@Autowired
	private INetworkPropertyManager							networkPropertyManager;

	@Autowired
	private IAppTokenManager								appTokenManager;

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "dev");
		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, PersonalDatabaseConfig.class);
			SPCollaboratorProfileUpdaterPerfRunner runner = new SPCollaboratorProfileUpdaterPerfRunner();
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

		String tenantId = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_TENANT);
		try {
			appTokenManager.initForTenant(tenantId);

			CollaboratorImportResult collaboratorImportResult = new CollaboratorImportResult();
			try {
				spCollabProfilePropertiesUpdater.updateCollaboratorProperties(LocalDate.now(), collaboratorImportResult);
			} catch (TryaneException | IOException e) {
				LOGGER.error("", e);
			}
		} finally {
			appTokenManager.clearForTenant(tenantId);
		}
	}
}
