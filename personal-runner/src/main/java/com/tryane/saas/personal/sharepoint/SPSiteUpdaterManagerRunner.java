package com.tryane.saas.personal.sharepoint;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.connector.common.manager.collaboratorid.IIDManager;
import com.tryane.saas.connector.o365.utils.token.IAppTokenManager;
import com.tryane.saas.connector.sharepoint.manager.sitecollection.SPSiteCollectionUpdateContext;
import com.tryane.saas.connector.sharepoint.manager.website.ISPSiteUpdaterManager;
import com.tryane.saas.connector.sharepoint.utils.context.SPSiteCollectionContext;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.core.sp.site.ISPSiteManager;
import com.tryane.saas.core.sp.site.SPSite;
import com.tryane.saas.core.sp.site.SPSitePK;
import com.tryane.saas.core.sp.sitecol.ISPSiteCollectionManager;
import com.tryane.saas.core.sp.sitecol.SPSiteCollection;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class SPSiteUpdaterManagerRunner extends AbstractSpringRunner {

	private static final Logger			LOGGER		= LoggerFactory.getLogger(SPSiteUpdaterManagerRunner.class);

	private final String				NETWORK_ID	= "s443708";

	private LocalDate					START_DATE	= LocalDate.now().minusDays(1);

	@Autowired
	private INetworkManager				networkManager;

	@Autowired
	private INetworkPropertyManager		networkPropertyManager;

	@Autowired
	private IAppTokenManager			appTokenManager;

	@Autowired
	private ISPSiteManager				siteManager;

	@Autowired
	private ISPSiteUpdaterManager		siteUpdaterManager;

	@Autowired
	private ISPSiteCollectionManager	siteCollectionManager;

	@Autowired
	private IIDManager					idManager;

	@Override
	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));

		String tenantId = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_TENANT);
		appTokenManager.initForTenant(tenantId);
		idManager.initForCurrentNetwork();

		SPSite webSite = siteManager.getSPSiteById(new SPSitePK("64cd4ffa-ddc2-4986-98e1-122f83ecc38c/d1cb5b46-381b-4961-9737-9e5ca868d5a7"));
		SPSiteCollection siteCollection = siteCollectionManager.getSPSiteCollectionById(webSite.getCollectionId());
		SPSiteCollectionUpdateContext spSiteCollectionUpdateContext = new SPSiteCollectionUpdateContext(START_DATE, tenantId, 1);
		SPSiteCollectionContext collectionContext = new SPSiteCollectionContext(siteCollection, appTokenManager.geAppTokenGenerator(SPSiteCollection.getRootUrl(siteCollection.getUrl()), tenantId));

		siteUpdaterManager.updateSite(webSite, START_DATE, spSiteCollectionUpdateContext, collectionContext);
	}

	public static void main(String[] args) {
		new SPSiteUpdaterManagerRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

}
