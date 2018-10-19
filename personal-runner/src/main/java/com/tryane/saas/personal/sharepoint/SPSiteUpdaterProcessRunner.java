package com.tryane.saas.personal.sharepoint;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.connector.o365.utils.token.IAppTokenManager;
import com.tryane.saas.connector.sharepoint.manager.sitecollection.ISPSiteCollectionUpdaterManager;
import com.tryane.saas.connector.sharepoint.manager.sitecollection.SPSiteCollectionUpdateContext;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.connector.context.ConnectorContext;
import com.tryane.saas.core.connector.execution.ConnectorExecution;
import com.tryane.saas.core.connector.stats.ConnectorStats;
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

public class SPSiteUpdaterProcessRunner extends AbstractSpringRunner {

	private static final Logger				LOGGER		= LoggerFactory.getLogger(SPSiteUpdaterProcessRunner.class);

	private final String					NETWORK_ID	= "s443708";

	private final LocalDate					startDate	= LocalDate.now().minusDays(1);

	@Autowired
	private INetworkManager					networkManager;

	@Autowired
	private INetworkPropertyManager			networkPropertyManager;

	@Autowired
	private IAppTokenManager				appTokenManager;

	@Autowired
	private ISPSiteManager					siteManager;

	@Autowired
	private ISPSiteCollectionManager		siteCollectionManager;

	@Autowired
	private ISPSiteCollectionUpdaterManager	siteCollectionUpdaterManager;

	@Override
	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));

		String tenantId = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_TENANT);
		appTokenManager.initForTenant(tenantId);

		SPSite website = siteManager.getSPSiteById(new SPSitePK("64cd4ffa-ddc2-4986-98e1-122f83ecc38c/7164d6e9-8d4a-42a3-98a5-5a6414d34e67"));
		SPSiteCollection siteCollection = siteCollectionManager.getSPSiteCollectionById(website.getCollectionId());
		SPSiteCollectionUpdateContext spSiteCollectionUpdateContext = new SPSiteCollectionUpdateContext(LocalDate.now().minusDays(1), tenantId, 1);

		ConnectorContext.init(new ConnectorExecution(), startDate, startDate, false, new ConnectorStats());

		try {
			siteCollectionUpdaterManager.updateSiteCollection(siteCollection, spSiteCollectionUpdateContext);
		} catch (InterruptedException e) {
			LOGGER.error("", e);
		}

		spSiteCollectionUpdateContext.getListIdsInDataBase().forEach(listId -> LOGGER.info("{}", listId));
	}

	public static void main(String[] args) {
		new SPSiteUpdaterProcessRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

}
