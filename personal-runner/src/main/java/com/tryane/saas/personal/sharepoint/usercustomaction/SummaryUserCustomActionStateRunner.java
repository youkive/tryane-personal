package com.tryane.saas.personal.sharepoint.usercustomaction;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.connector.o365.utils.authentication.IO365Authenticator;
import com.tryane.saas.connector.o365.utils.exception.O365UserAuthenticationException;
import com.tryane.saas.connector.sharepoint.manager.injection.IInjectionManager;
import com.tryane.saas.core.AbstractSpringRunner;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.core.sp.sitecol.ISPSiteCollectionManager;
import com.tryane.saas.core.sp.sitecol.SPSiteCollection;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

import jersey.repackaged.com.google.common.collect.Lists;

public class SummaryUserCustomActionStateRunner extends AbstractSpringRunner {

	private static final Logger			LOGGER		= LoggerFactory.getLogger(SummaryUserCustomActionStateRunner.class);

	private static final String			NETWORK_ID	= "s1";

	@Autowired
	private ISPSiteCollectionManager	siteCollectionManager;

	@Autowired
	private INetworkManager				networkManager;

	@Autowired
	private INetworkPropertyManager		networkPropertyManager;

	@Autowired
	private IInjectionManager			injectionManager;

	@Autowired
	private IO365Authenticator			authenticator;

	public static void main(String[] args) {
		new SummaryUserCustomActionStateRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

	@Override
	protected void testImplementation() {
		try {
			ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));
			List<SiteCollectionState> states = Lists.newArrayList();
			String token = getToken();
			siteCollectionManager.getAllMonitoredSiteCollections().forEach(siteCollection -> {
				SiteCollectionState state = new SiteCollectionState(siteCollection);
				state.isMonitoredByJs = injectionManager.siteCollectionIsMonitoredByJsAgent(siteCollection.getId(), token);
				state.isMonitoredBySpfxAgent = injectionManager.siteCollectionIsMonitoredByAddonAgent(siteCollection.getId(), token);
				states.add(state);
			});
			displayResult(states);
		} catch (O365UserAuthenticationException e) {
			LOGGER.error("", e);
		}
	}

	private String getToken() throws O365UserAuthenticationException {
		String tenant = networkPropertyManager.getNetworkPropertyValue(ClientContextHolder.getNetworkId(), NetworkPropertyNames.SHAREPOINT_TENANT);
		String sharepointUrl = networkPropertyManager.getNetworkPropertyValue(ClientContextHolder.getNetworkId(), NetworkPropertyNames.SHAREPOINT_MAINCOLLECTION_URL);
		return authenticator.getAppAccessToken(sharepointUrl, tenant).getAccessToken();
	}

	private void displayResult(List<SiteCollectionState> states) {
		states.forEach(state -> {
			LOGGER.info(state.siteCollection.getUrl());
			LOGGER.info("isMonitoredByJs : {}", state.isMonitoredByJs);
			LOGGER.info("isMonitoredByAddonAgent : {}", state.isMonitoredBySpfxAgent);
		});
	}

	class SiteCollectionState {
		SPSiteCollection	siteCollection			= null;

		Boolean				isMonitoredByJs			= false;

		Boolean				isMonitoredBySpfxAgent	= false;

		public SiteCollectionState(SPSiteCollection siteCollection) {
			this.siteCollection = siteCollection;
		}
	}
}
