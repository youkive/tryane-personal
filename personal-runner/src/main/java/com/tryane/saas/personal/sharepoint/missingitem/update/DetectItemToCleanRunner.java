package com.tryane.saas.personal.sharepoint.missingitem.update;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.connector.sharepoint.manager.items.ISPItemUtils;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.sp.item.ISPItemManager;
import com.tryane.saas.core.sp.list.ISPListManager;
import com.tryane.saas.core.sp.list.SPList;
import com.tryane.saas.core.sp.list.SPListPK;
import com.tryane.saas.core.sp.site.ISPSiteManager;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class DetectItemToCleanRunner extends AbstractSpringRunner {

	private static final Logger	LOGGER		= LoggerFactory.getLogger(DetectItemToCleanRunner.class);

	private final String		NETWORK_ID	= "s443632";

	@Autowired
	private INetworkManager		networkManager;

	@Autowired
	private ISPItemUtils		itemUtils;

	@Autowired
	private ISPItemManager		itemManager;

	@Autowired
	private ISPListManager		listManager;

	@Autowired
	private ISPSiteManager		siteManager;

	@Override
	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));

		Set<SPList> listNotTargeted = new HashSet<>();
		siteManager.processAllSites(site -> {
			listManager.getAllListsInWebSite(site.getSitePK()).stream().filter(list -> !itemUtils.isTargetListToSaveItem(list)).forEach(list -> listNotTargeted.add(list));
		});
		Set<SPListPK> listPkNotTargeted = listNotTargeted.stream().map(SPList::getSpListPK).collect(Collectors.toSet());

		AtomicLong count = new AtomicLong(0);
		itemManager.processAllItems(item -> {
			SPListPK listPk = new SPListPK(item.getId().split("/")[0], item.getSiteId());
			if (listPkNotTargeted.contains(listPk)) {
				count.incrementAndGet();
			}
		});

		LOGGER.info("{} items to clean from the database", count.get());
	}

	public static void main(String[] args) {
		new DetectItemToCleanRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

}
