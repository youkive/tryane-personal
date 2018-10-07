package com.tryane.saas.personal.sharepoint.missingitem.update;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.esn.item.EsnItemProperties;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.sp.item.ISPItemManager;
import com.tryane.saas.core.sp.item.SPItem;
import com.tryane.saas.core.sp.item.SPItemPK;
import com.tryane.saas.core.sp.item.SPItemPropertiesNames;
import com.tryane.saas.core.sp.site.ISPSiteManager;
import com.tryane.saas.core.sp.site.SPSite;
import com.tryane.saas.core.sp.site.SPSitePK;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;
import com.tryane.saas.results.TimeScale;
import com.tryane.saas.results.availability.IResultAvailabilityManager;
import com.tryane.saas.results.common.manager.IndicatorResultsPerimeter;
import com.tryane.saas.results.common.manager.ResultsAtDateHolder;
import com.tryane.saas.results.common.top.TopUnitIndicatorResult.TopEntry;
import com.tryane.saas.results.sharepoint.site.indicator.SPSiteIndicators;
import com.tryane.saas.results.sharepoint.site.results.bysite.manager.ISPSiteResultsManager;
import com.tryane.saas.results.sharepoint.site.results.bysite.perimeter.SPSiteResultsAtDatePerimeter;
import com.tryane.saas.utils.joda.JodaUtils;
import com.tryane.saas.utils.string.StringUtils;

public class ItemDeletedDateUpdateRunner extends AbstractSpringRunner {

	private static final Logger			LOGGER		= LoggerFactory.getLogger(ItemDeletedDateUpdateRunner.class);

	private static final String			NETWORK_ID	= "s443673";

	@Autowired
	private INetworkManager				networkManager;

	@Autowired
	private ISPItemManager				itemManager;

	@Autowired
	@Qualifier("siteResultsManager")
	private ISPSiteResultsManager		siteResultsManager;

	@Autowired
	private IResultAvailabilityManager	resultAvaibilityManager;

	@Autowired
	private ISPSiteManager				siteManager;

	@Override
	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));

		AtomicLong countStrangeNoDate = new AtomicLong(0L);
		AtomicLong countToUpdate = new AtomicLong(0L);
		AtomicLong countItemStrangeCreationDate = new AtomicLong(0);
		AtomicLong countItemStrangeNoCreationDate = new AtomicLong(0);
		itemManager.processAllItems(item -> {
			String dateAsString = item.getDataValue(SPItemPropertiesNames.DELETED_AT);
			if (StringUtils.isNullOrEmpty(dateAsString)) {
				countStrangeNoDate.incrementAndGet();
				return;
			}
			LocalDate dateDeletion = LocalDate.parse(dateAsString);
			if (dateDeletion.equals(LocalDate.parse("2018-10-05"))) {
				countToUpdate.incrementAndGet();
				return;
			}
		});

		Set<SPSitePK> sitePksInItem = new HashSet<>();
		itemManager.processAllItems(item -> {
			sitePksInItem.add(new SPSitePK(item.getSpItemPK().getSiteId()));
		});

		LocalDate[] resultsAvaibility = resultAvaibilityManager.getWeeklyResultsAvailabilityPeriod(NETWORK_ID);
		AtomicLong itemDocWithDateAvailable = new AtomicLong(0);
		AtomicLong itemPageWithDateAvailable = new AtomicLong(0);
		AtomicLong itemAssetWithDateAvailable = new AtomicLong(0);
		for (SPSitePK siteIdPk : sitePksInItem) {
			SPSite currentSite = siteManager.getSPSiteById(siteIdPk);
			Set<SPItem> itemToUpdate = new HashSet<>();
			itemManager.processAllItemsForSite(siteIdPk, item -> {
				if (StringUtils.isNotNullNorEmpty(item.getDataValue(SPItemPropertiesNames.DELETED_AT))) {
					itemToUpdate.add(item);
				}
			});
			if (itemToUpdate.isEmpty()) {
				continue;
			} else {
				LOGGER.info("{} items to update for site {}", itemToUpdate.size(), siteIdPk.getCombinedSiteId());
			}
			Set<SPItemPK> itemPkToUpdate = itemToUpdate.stream().map(SPItem::getSpItemPK).collect(Collectors.toSet());

	
			LocalDate loopDate = resultsAvaibility[1];
			if (currentSite.getDeletionDate() != null) {
				loopDate = JodaUtils.getWeekIdentifier(JodaUtils.getWeekIdentifier(currentSite.getDeletionDate()));
			}
			while (!loopDate.isBefore(resultsAvaibility[0]) && !itemPkToUpdate.isEmpty()) {
				ResultsAtDateHolder resultsAtDate = siteResultsManager.getResultsAtDate(new SPSiteResultsAtDatePerimeter(TimeScale.WEEK, siteIdPk, loopDate, 1, false));
				List<TopEntry> entriesTopDoc = resultsAtDate.getTopValues(new IndicatorResultsPerimeter().withIndicator(SPSiteIndicators.TOP_DOC_BY_VIEW));
				if (entriesTopDoc != null) {
					for (TopEntry entriesTop : entriesTopDoc) {
						String entryKey = entriesTop.getKey();
						SPItemPK itemPkForEntry = new SPItemPK(entryKey, currentSite.getCombinedSiteId());
						if (itemPkToUpdate.remove(itemPkForEntry)) {
							itemDocWithDateAvailable.incrementAndGet();
						}
					}
				}
				
				List<TopEntry> entriesTopPage = resultsAtDate.getTopValues(new IndicatorResultsPerimeter().withIndicator(SPSiteIndicators.TOP_PAGE_BY_VIEW));
				if (entriesTopPage != null) {
					for (TopEntry entriesTop : entriesTopPage) {
						String entryKey = entriesTop.getKey();
						SPItemPK itemPkForEntry = new SPItemPK(entryKey, currentSite.getCombinedSiteId());
						if (itemPkToUpdate.remove(itemPkForEntry)) {
							itemPageWithDateAvailable.incrementAndGet();
						}
					}
				}
				
				List<TopEntry> entriesTopAsset = resultsAtDate.getTopValues(new IndicatorResultsPerimeter().withIndicator(SPSiteIndicators.TOP_ASSET_BY_VIEW));
				if (entriesTopPage != null) {
					for (TopEntry entriesTop : entriesTopAsset) {
						String entryKey = entriesTop.getKey();
						SPItemPK itemPkForEntry = new SPItemPK(entryKey, currentSite.getCombinedSiteId());
						if (itemPkToUpdate.remove(itemPkForEntry)) {
							itemAssetWithDateAvailable.incrementAndGet();
						}
					}
				}
				
				loopDate = loopDate.minusWeeks(1);
			}
			for(SPItemPK itemPkNotFound : itemPkToUpdate) {
				LOGGER.info("{}/{}", itemPkNotFound.getSiteId(), itemPkNotFound.getId());
				SPItem itemAssociated = itemManager.getItem(itemPkNotFound.getId().split("/")[0], itemPkNotFound.getId().split("/")[1], new SPSitePK(itemPkNotFound.getSiteId()));
				if(StringUtils.isNullOrEmpty(itemAssociated.getDataValue("creationdate"))) {
					countItemStrangeNoCreationDate.incrementAndGet();
					continue;
				}
				LocalDate dateCreation = LocalDate.parse(itemAssociated.getDataValue("creationdate"));
				if(dateCreation.isBefore(LocalDate.parse("2018-05-09"))) {
					countItemStrangeCreationDate.incrementAndGet();
				}
			}
			
		}
		

		LOGGER.info("{} item strange", countStrangeNoDate.get());
		LOGGER.info("{} items to update with old creation date", countItemStrangeCreationDate.get());
		LOGGER.info("{} items to update with no creation date", countItemStrangeNoCreationDate.get());
		LOGGER.info("{} item to update", countToUpdate.get());
		LOGGER.info("{} DOC updated", itemDocWithDateAvailable.get());
		LOGGER.info("{} PAGE updated", itemPageWithDateAvailable.get());
		LOGGER.info("{} ASSETS updated", itemAssetWithDateAvailable.get());
		LOGGER.info("");
	}

	public static void main(String[] args) {
		new ItemDeletedDateUpdateRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

}
