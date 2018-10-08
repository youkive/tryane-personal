package com.tryane.saas.personal.sharepoint.missingitem.update;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.connector.o365.utils.exception.O365UserAuthenticationException;
import com.tryane.saas.connector.o365.utils.token.IAppTokenManager;
import com.tryane.saas.connector.sharepoint.utils.api.ISPWebListAPI;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.core.sp.item.ISPItemManager;
import com.tryane.saas.core.sp.item.SPItem;
import com.tryane.saas.core.sp.item.SPItemPK;
import com.tryane.saas.core.sp.item.SPItemPropertiesNames;
import com.tryane.saas.core.sp.list.ISPListManager;
import com.tryane.saas.core.sp.list.ListBaseTemplate;
import com.tryane.saas.core.sp.list.SPList;
import com.tryane.saas.core.sp.site.ISPSiteManager;
import com.tryane.saas.core.sp.site.SPSite;
import com.tryane.saas.core.sp.site.SPSitePK;
import com.tryane.saas.core.sp.sitecol.SPSiteCollection;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;
import com.tryane.saas.utils.hibernate.SingleTransactionHibernateBatch;
import com.tryane.saas.utils.multithreading.TryaneThreadFactoryBuilder;

/**
 * Mise à jour des items en base avec les champs is_deleted (Boolean) et list_template
 * runner associé à SAAS-3200
 */
public class UpdateNewContentTopItemRunner extends AbstractSpringRunner {

	private static final Logger		LOGGER		= LoggerFactory.getLogger(UpdateNewContentTopItemRunner.class);

	private String					NETWORK_ID	= "s443673";

	@Autowired
	private INetworkManager			networkManager;

	@Autowired
	private ISPItemManager			itemManager;

	@Autowired
	private ISPListManager			listManager;

	@Autowired
	private ISPSiteManager			siteManager;

	@Autowired
	private IAppTokenManager		appTokenManager;

	@Autowired
	private INetworkPropertyManager	networkPropertyManager;

	@Autowired
	private ISPWebListAPI			webListApi;

	@Override
	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));
		String tenantId = networkPropertyManager.getNetworkPropertyValue(ClientContextHolder.getNetworkId(), NetworkPropertyNames.SHAREPOINT_TENANT);
		appTokenManager.initForTenant(tenantId);

		ExecutorService threadPool = Executors.newFixedThreadPool(20, new TryaneThreadFactoryBuilder("update-item-" + ClientContextHolder.getNetworkId()).build());
		listManager.processAllValidLists(list -> {
			threadPool.execute(new ItemListUpdaterRunnable(list, tenantId));
		});
		try {
			threadPool.shutdown();
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (Exception e) {
			LOGGER.error("", e);
		}

		appTokenManager.clearForTenant(tenantId);
	}

	public void flush(Set<SPItemPK> itemPkToAnalyze) {

		itemPkToAnalyze.clear();
	}

	class ItemListUpdaterRunnable implements Runnable {

		private SPList	list;

		private String	tenantId;

		public ItemListUpdaterRunnable(SPList list, String tenantId) {
			this.list = list;
			this.tenantId = tenantId;
		}

		@Override
		public void run() {

			try {
				if (!isTargetListToSaveItem(list.getBaseTemplate())) {
					return;
				}

				LOGGER.info("analyse list {}", list.getSpListPK().getListId());
				List<SPItem> itemsInDb = itemManager.getItemsIn(list);
				Set<SPItemPK> itemPkInDb = itemsInDb.stream().map(SPItem::getSpItemPK).collect(Collectors.toSet());
				SPSite website = siteManager.getSPSiteById(new SPSitePK(list.getSpListPK().getSiteId()));
				SingleTransactionHibernateBatch<SPItem> itemBatch = itemManager.createItemBatch();
				webListApi.procesAllItemsInList(website.getUrl(), appTokenManager.geAppTokenGenerator(SPSiteCollection.getRootUrl(website.getUrl()), tenantId).getToken(), list.getSpListPK().getListId(), itemFromSP -> {
					SPItemPK itemPKFromSP = new SPItemPK(list.getSpListPK().getListId() + "/" + itemFromSP.getId(), website.getCombinedSiteId());
					Optional<SPItem> itemInDbOpt = itemsInDb.stream().filter(item -> item.getSpItemPK().equals(itemPKFromSP)).findFirst();
					if (itemInDbOpt.isPresent()) {
						// update item
						itemInDbOpt.get().setDataValue(SPItemPropertiesNames.LIST_TEMPLATE, itemFromSP.getParentList().getBaseTemplate().toString());
						itemBatch.addToUpdate(itemInDbOpt.get());
						itemPkInDb.remove(itemPKFromSP);
					}
				});

				for (SPItemPK itemPkDeleted : itemPkInDb) {
					SPItem itemInDb = itemsInDb.stream().filter(item -> item.getSpItemPK().equals(itemPkDeleted)).findFirst().get();
					itemInDb.setDataValue(SPItemPropertiesNames.LIST_TEMPLATE, list.getBaseTemplate().toString());
					itemInDb.setDataValue(SPItemPropertiesNames.DELETED_AT, LocalDate.now().toString());
					itemBatch.addToUpdate(itemInDb);
				}

				itemBatch.flushAll();
			} catch (O365ConnectionException | O365HttpErrorException | O365UserAuthenticationException e) {
				LOGGER.debug("", e);
			} catch (Throwable e) {
				LOGGER.error("", e);
			}
		}

	}

	public static void main(String[] args) {
		new UpdateNewContentTopItemRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

	public boolean isTargetListToSaveItem(Integer baseTemplate) {
		return baseTemplate == ListBaseTemplate.PAGES || baseTemplate == ListBaseTemplate.WEBPAGELIBRARY || baseTemplate == ListBaseTemplate.PICTURELIBRARY || baseTemplate == ListBaseTemplate.ASSET_LIBRARY_VIDEO_CHANNEL || baseTemplate == ListBaseTemplate.DOCUMENTLIBRARY;
	}
}
