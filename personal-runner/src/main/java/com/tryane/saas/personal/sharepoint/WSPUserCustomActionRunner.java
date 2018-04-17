package com.tryane.saas.personal.sharepoint;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.connector.o365.utils.exception.O365UserAuthenticationException;
import com.tryane.saas.connector.o365.utils.token.IAppTokenManager;
import com.tryane.saas.connector.sharepoint.sitecollections.jsinjection.ISPJSInjectionManagerUtils;
import com.tryane.saas.connector.sharepoint.utils.api.ISPSiteAPI;
import com.tryane.saas.connector.sharepoint.utils.model.SPSiteUserCustomAction;
import com.tryane.saas.core.AbstractSpringRunner;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.core.sp.sitecol.ISPSiteCollectionManager;
import com.tryane.saas.core.sp.sitecol.SPSiteCollection;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;
import com.tryane.saas.utils.string.StringUtils;

public class WSPUserCustomActionRunner extends AbstractSpringRunner {

	private static final Logger			LOGGER		= LoggerFactory.getLogger(WSPUserCustomActionRunner.class);

	private static final String			NETWORK_ID	= "s140";

	@Autowired
	private INetworkManager				networkManager;

	@Autowired
	private ISPSiteCollectionManager	siteCollectionManager;

	@Autowired
	private IAppTokenManager			appTokenManager;

	@Autowired
	private INetworkPropertyManager		networkPropertyManager;

	@Autowired
	private ISPSiteAPI					siteApi;

	public static void main(String[] args) {
		new WSPUserCustomActionRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

	@Override
	protected void testImplementation() {
		Network currentNetwork = networkManager.getNetworkById(NETWORK_ID);
		ClientContextHolder.setNetwork(currentNetwork);

		String tenantId = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_TENANT);
		String spUrl = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_MAINCOLLECTION_URL);
		appTokenManager.initForTenant(tenantId);
		List<SPSiteCollection> siteCollections = siteCollectionManager.getAllMonitoredSiteCollections();
		AtomicLong count = new AtomicLong(0);
		AtomicLong countTryaneMonitoredSC = new AtomicLong(0);
		siteCollections.forEach(siteCollection -> {
			try {
				List<SPSiteUserCustomAction> ucas = siteApi.getAllUserCustomActions(siteCollection.getUrl(), appTokenManager.geAppTokenGenerator(spUrl, tenantId).getToken());
				ucas.stream().filter(uca -> isWSPUca(uca)).forEach(uca -> {
					LOGGER.info("ICI found {}", siteCollection.getUrl());
				});
				Long nbUcaTryane = ucas.stream().filter(uca -> isTryaneNewAgent(uca)).count();
				if (nbUcaTryane > 0) {
					countTryaneMonitoredSC.incrementAndGet();
				}
			} catch (O365ConnectionException | O365HttpErrorException | O365UserAuthenticationException e) {
				LOGGER.error("", e);
			} finally {
				Long currentCount = count.incrementAndGet();
				if (currentCount % 100 == 0) {
					LOGGER.info("analyze {} sitecollections", currentCount);
				}
			}
		});

		appTokenManager.clearForTenant(tenantId);
		LOGGER.info("found {} site collections monitored", countTryaneMonitoredSC.get());
	}

	private Boolean isWSPUca(SPSiteUserCustomAction uca) {
		String tryaneServerUrl = "https://analytics.tryane.com";
		return StringUtils.isNotNullNorEmpty(uca.getScriptBlock()) && uca.getScriptBlock().contains(tryaneServerUrl) && StringUtils.isNotNullNorEmpty(uca.getTitle()) && !uca.getTitle().equals(ISPJSInjectionManagerUtils.buildTitleOfUserCustomAction(tryaneServerUrl));
	}

	private Boolean isTryaneNewAgent(SPSiteUserCustomAction uca) {
		String tryaneServerUrl = "https://analytics.tryane.com";
		return StringUtils.isNotNullNorEmpty(uca.getScriptBlock()) && uca.getScriptBlock().contains(tryaneServerUrl) && (StringUtils.isNullOrEmpty(uca.getTitle()) || uca.getTitle().equals(ISPJSInjectionManagerUtils.buildTitleOfUserCustomAction(tryaneServerUrl)));
	}
}
