package com.tryane.saas.personal.sharepoint.missingitem.update;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.sp.item.ISPItemManager;
import com.tryane.saas.core.sp.item.SPItemPropertiesNames;
import com.tryane.saas.core.sp.list.ISPListManager;
import com.tryane.saas.core.sp.list.SPList;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;
import com.tryane.saas.utils.string.StringUtils;

public class ItemPostActionAnalyzeRunner extends AbstractSpringRunner {

	private static final Logger	LOGGER		= LoggerFactory.getLogger(ItemPostActionAnalyzeRunner.class);

	private static String		NETWORK_ID	= "s443673";

	@Autowired
	private INetworkManager		networkManager;

	@Autowired
	private ISPItemManager		itemManager;

	@Autowired
	private ISPListManager		listManager;

	@Override
	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));
		AtomicLong total = new AtomicLong(0);
		AtomicLong notUpdatedCount = new AtomicLong(0);
		Set<SPList> listIdStrange = new HashSet<>();
		Set<SPList> listNotDeleted = new HashSet<>();
		itemManager.processAllItems(item -> {
			total.incrementAndGet();
			if (!(StringUtils.isNotNullNorEmpty(item.getDataValue(SPItemPropertiesNames.LIST_TEMPLATE)) || StringUtils.isNotNullNorEmpty(item.getDataValue(SPItemPropertiesNames.DELETED_AT)))) {
				notUpdatedCount.incrementAndGet();
				SPList list = listManager.getList(item.getSiteId(), item.getId().split("/")[0]);
				if (list == null) {
					//listIdStrange.add(list);
				} else if (list.getDeletionDate() == null) {
					listNotDeleted.add(list);
				} else {
					listIdStrange.add(list);
				}
			}
		});
		for (SPList list : listIdStrange) {
			LOGGER.error("Strange not found list {}/{}", list.getSpListPK().getSiteId(), list.getSpListPK().getListId());
		}
		for (SPList list : listNotDeleted) {
			LOGGER.error("List not deleted {}/{}", list.getSpListPK().getSiteId(), list.getSpListPK().getListId());
		}
		LOGGER.info("{} not updated on {} items", notUpdatedCount.get(), total.get());
	}

	public static void main(String[] args) {
		new ItemPostActionAnalyzeRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}
}
