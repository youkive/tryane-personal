package com.tryane.saas.personal.sharepoint.missingitem;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.connector.sharepoint.items.ISPItemUtils;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.sp.item.ISPItemManager;
import com.tryane.saas.core.sp.item.SPItem;
import com.tryane.saas.core.sp.item.SPItemPK;
import com.tryane.saas.core.sp.list.ISPListManager;
import com.tryane.saas.core.sp.list.SPListPK;
import com.tryane.saas.core.sp.site.SPSitePK;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;
import com.tryane.saas.utils.hibernate.SingleTransactionHibernateBatch;

/**
 * Cleaner les items qui n'ont rien à faire en db
 */
public class CleanItemDBRunner extends AbstractSpringRunner {

	private static final Logger	LOGGER		= LoggerFactory.getLogger(CleanItemDBRunner.class);

	private static final String	NETWORK_ID	= "s11";

	@Autowired
	private INetworkManager		networkManager;

	@Autowired
	private ISPItemManager		itemManager;

	@Autowired
	private ISPListManager		listManager;

	@Autowired
	private ISPItemUtils		itemUtils;

	@Override
	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));

		Set<SPListPK> listPksWithNoItems = new HashSet<>();
		listManager.processAllValidLists(list -> {
			if (!itemUtils.isTargetListToSaveItem(list)) {
				listPksWithNoItems.add(list.getSpListPK());
			}
		});
		
		AtomicLong count = new AtomicLong(0);
		SingleTransactionHibernateBatch<SPItem> itemBatch = itemManager.createItemBatch();
		// on process les items sur les sites à parcourir pour éviter un OOM au parcours de tous les items
		Set<SPSitePK> sitesPK = listPksWithNoItems.stream().map(listPK -> new SPSitePK(listPK.getSiteId())).collect(Collectors.toSet());
		for(SPSitePK sitePK : sitesPK) {
			itemManager.processAllItemsForSite(sitePK, item -> {
				SPItemPK itemPK = item.getSpItemPK();
				SPListPK associatedListPk = new SPListPK(itemPK.getId().split("/")[0], itemPK.getSiteId());
				if (listPksWithNoItems.contains(associatedListPk)) {
					count.incrementAndGet();
					itemBatch.addToDelete(item);
				}
			});
		}
		itemBatch.flushAll();

		LOGGER.info("{} items deleted", count.get());
	}

	public static void main(String[] args) {
		new CleanItemDBRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

}
