package com.tryane.saas.personal.sharepoint;

import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.sp.site.ISPSiteManager;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

import nl.basjes.shaded.com.google.common.base.Joiner;
import nl.basjes.shaded.com.google.common.collect.Lists;

public class SPWebSiteRunner extends AbstractSpringRunner {

	private static final Logger	LOGGER						= LoggerFactory.getLogger(SPWebSiteRunner.class);

	private static final String	SITE_COLLECTION_ID_FILTER	= "594a661a-8f64-4769-8ea3-48b2331623af";
	private static final String	NETWORK_ID					= "s443520";
	@Autowired
	private INetworkManager		networkManager;

	@Autowired
	private ISPSiteManager		siteManager;

	@Override
	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));
		List<String> siteIdsFiltered = Lists.newArrayList();
		siteManager.processAllSites(webSite -> {
			if (!webSite.getSitePK().getCollectionId().equals(SITE_COLLECTION_ID_FILTER)) {
				return;
			}
			siteIdsFiltered.add("'" + webSite.getCombinedSiteId() + "'");
		}, LocalDate.now());

		LOGGER.info("nb Web Sites : {}", siteIdsFiltered.size());
		LOGGER.info("");
		LOGGER.info("{}", Joiner.on(",").join(siteIdsFiltered));
	}

	public static void main(String[] args) {
		new SPWebSiteRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}
}
