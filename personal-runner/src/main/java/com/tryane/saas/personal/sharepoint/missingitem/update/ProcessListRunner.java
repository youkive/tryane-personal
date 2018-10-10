package com.tryane.saas.personal.sharepoint.missingitem.update;

import java.util.concurrent.atomic.AtomicLong;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.connector.o365.utils.exception.O365UserAuthenticationException;
import com.tryane.saas.connector.o365.utils.token.IAppTokenManager;
import com.tryane.saas.connector.o365.utils.token.IO365TokenSupplier;
import com.tryane.saas.connector.sharepoint.utils.api.ISPWebListAPI;
import com.tryane.saas.connector.sharepoint.utils.model.SharepointSPItem;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;
import com.tryane.saas.utils.hibernate.ICallBack;

public class ProcessListRunner extends AbstractSpringRunner {

	private static final Logger		LOGGER		= LoggerFactory.getLogger(ProcessListRunner.class);

	private static final String		NETWORK_ID	= "s443632";

	private static final String		WEBSITE_URL	= "https://premierhealth.sharepoint.com/sites/apps/policies";

	private static final String		LIST_ID		= "dfa53847-b374-4d12-b3b6-e63fcda6d532";

	@Autowired
	private INetworkManager			networkManager;

	@Autowired
	private INetworkPropertyManager	networkPropertyManager;

	@Autowired
	private IAppTokenManager		appTokenManager;

	@Autowired
	private ISPWebListAPI			webListApi;

	@Override
	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));
		String tenantId = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_TENANT);

		appTokenManager.initForTenant(tenantId);
		AtomicLong count = new AtomicLong(0);
		AtomicLong countAfter = new AtomicLong(0);
		try {
			webListApi.procesAllItemsInListCreatedAfter(LocalDate.parse("2018-10-01"), WEBSITE_URL, new IO365TokenSupplier() {

				@Override
				public String getToken() throws O365UserAuthenticationException {
					return appTokenManager.geAppTokenGenerator("https://premierhealth.sharepoint.com", tenantId).getToken();
				}
			}, LIST_ID, new ICallBack<SharepointSPItem>() {

				@Override
				public void processObject(SharepointSPItem item) {
					countAfter.incrementAndGet();
				}
			});

			webListApi.processAllItemsInListCreatedBefore(LocalDate.now(), WEBSITE_URL, new IO365TokenSupplier() {

				@Override
				public String getToken() throws O365UserAuthenticationException {
					return appTokenManager.geAppTokenGenerator("https://premierhealth.sharepoint.com", tenantId).getToken();
				}
			}, LIST_ID, new ICallBack<SharepointSPItem>() {

				@Override
				public void processObject(SharepointSPItem objectToProcess) {
					count.incrementAndGet();
				}
			});
		} catch (O365ConnectionException | O365HttpErrorException | O365UserAuthenticationException e) {
			LOGGER.error("", e);
		}

		LOGGER.info("retrieve {} items", count.get());
		LOGGER.info("retrive {} items  after", countAfter.get());
	}

	public static void main(String[] args) {
		new ProcessListRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}
}
