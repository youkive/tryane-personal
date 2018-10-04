package com.tryane.saas.personal.sharepoint.missingitem;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.connector.o365.utils.token.IAppTokenManager;
import com.tryane.saas.connector.sharepoint.items.missingitems.ISPRecoverEmptyAndMissingItemsProcess;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class UpdateMissingItemOnEventsRunner extends AbstractSpringRunner {

	private static final String						NETWORK_ID	= "s443673";

	private static final LocalDate					START_DATE	= LocalDate.parse("2018-08-01");

	private static final LocalDate					END_DATE	= LocalDate.parse("2018-09-30");

	@Autowired
	private INetworkManager							networkManager;

	@Autowired
	private ISPRecoverEmptyAndMissingItemsProcess	recoverProcess;

	@Autowired
	private IAppTokenManager						appTokenManager;

	@Autowired
	private INetworkPropertyManager					networkPropertyManager;

	@Override
	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));

		String tenantId = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_TENANT);
		appTokenManager.initForTenant(tenantId);

		recoverProcess.checkForMissingItemsOnPeriod(START_DATE, END_DATE);
	}

	public static void main(String[] args) {
		new UpdateMissingItemOnEventsRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

}
