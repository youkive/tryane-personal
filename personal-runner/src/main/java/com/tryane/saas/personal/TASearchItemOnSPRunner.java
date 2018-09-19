package com.tryane.saas.personal;

import java.util.Set;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;
import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.connector.o365.utils.exception.O365UserAuthenticationException;
import com.tryane.saas.connector.o365.utils.token.IAppTokenManager;
import com.tryane.saas.connector.sharepoint.utils.api.ISPWebListAPI;
import com.tryane.saas.connector.sharepoint.utils.model.SharepointSPItem;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;
import com.tryane.saas.utils.hibernate.ICallBack;

public class TASearchItemOnSPRunner extends AbstractSpringRunner {

	private static final Logger		LOGGER		= LoggerFactory.getLogger(TASearchItemOnSPRunner.class);

	private final String			NETWORK_ID	= "s443662";

	@Autowired
	private ISPWebListAPI			webListApi;

	@Autowired
	private INetworkManager			networkManager;

	@Autowired
	private INetworkPropertyManager	networkPropertyManager;

	@Autowired
	private IAppTokenManager		appTokenManager;

	public static void main(String[] args) {
		new TASearchItemOnSPRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));

		String tenantId = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_TENANT);
		String spUrl = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_MAINCOLLECTION_URL);
		appTokenManager.initForTenant(tenantId);

		try {
			Set<String> found = Sets.newHashSet();
			webListApi.procesAllItemsInListCreatedAfter(new LocalDate(0), "https://goairrosti.sharepoint.com", appTokenManager.geAppTokenGenerator(spUrl, tenantId).getToken(), "5cee0542-b377-48da-b19b-340ea9e3a0d1", new ICallBack<SharepointSPItem>() {

				@Override
				public void processObject(SharepointSPItem item) {
					String pageUrl = item.getSpItemProperties().getFileRef();
					found.add(pageUrl);
				}
			});
			found.forEach(url -> {
				LOGGER.info(url);
			});
		} catch (O365ConnectionException | O365HttpErrorException | O365UserAuthenticationException e) {
			LOGGER.error("", e);
		}
	}

}
