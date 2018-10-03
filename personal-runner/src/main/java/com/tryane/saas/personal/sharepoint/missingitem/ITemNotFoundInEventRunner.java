package com.tryane.saas.personal.sharepoint.missingitem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.csvreader.CsvWriter;
import com.google.common.collect.Lists;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.event.Event;
import com.tryane.saas.core.event.IEventCallBack;
import com.tryane.saas.core.event.IEventManager;
import com.tryane.saas.core.event.SPEventType;
import com.tryane.saas.core.event.props.SPEventPropertyNames;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.core.period.Day;
import com.tryane.saas.core.sp.item.ISPItemManager;
import com.tryane.saas.core.sp.item.SPItemPK;
import com.tryane.saas.core.sp.list.ISPListManager;
import com.tryane.saas.core.sp.list.SPList;
import com.tryane.saas.core.sp.list.SPListPK;
import com.tryane.saas.core.sp.site.SPSitePK;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;
import com.tryane.saas.results.sharepoint.site.category.ISPActionCategoryManager;

public class ITemNotFoundInEventRunner extends AbstractSpringRunner {
	private static final String			NETWORK_ID			= "s443673";

	private static final Logger			LOGGER				= LoggerFactory.getLogger(ITemNotFoundInEventRunner.class);

	private static final LocalDate[]	ANALYSIS_PERIOD		= new LocalDate[] { LocalDate.parse("2018-05-10"), LocalDate.parse("2018-05-29") };

	private static final String			RESSOURCE_FOLDER	= "src/test/resources/com/tryane/saas/results";

	@Autowired
	private ISPItemManager				spItemManager;

	@Autowired
	private INetworkManager				networkManager;

	@Autowired
	private ISPListManager				spListManager;

	@Autowired
	private ISPActionCategoryManager	spActionCategoryManager;

	@Autowired
	private IEventManager				eventManager;

	@Override
	protected void testImplementation() {
		Network network = networkManager.getNetworkById(NETWORK_ID);
		ClientContextHolder.setNetwork(network);

		MyCallback itemAcumulator = new MyCallback();

		// parse events, every day
		LocalDate loopDate = ANALYSIS_PERIOD[0];
		while (!loopDate.isAfter(ANALYSIS_PERIOD[1])) {
			LOGGER.info("Processing events for {}", loopDate);
			eventManager.processEventsOnPeriod(itemAcumulator, new Day(loopDate));
			LOGGER.info("Processed events for {}", loopDate);
			loopDate = loopDate.plusDays(1);
		}

		int totalItemCount = itemAcumulator.itemCount.size();
		int loadBy = 10000;

		LOGGER.info("Period: {} to {}", ANALYSIS_PERIOD[0], ANALYSIS_PERIOD[1]);
		LOGGER.info("Identified {} items in {} events", totalItemCount, itemAcumulator.itemCount.values().stream().map(Counter::getNbEvent).reduce(Long::sum).get());

		// identify unknown items
		List<List<SPItemPK>> splittedItemIds = Lists.partition(new ArrayList<>(itemAcumulator.itemCount.keySet()), loadBy);
		// process batch of items
		int processed = 0;
		for (List<SPItemPK> itemIdList : splittedItemIds) {
			spItemManager.getItemsIn(itemIdList).forEach(i -> itemAcumulator.itemCount.remove(i.getSpItemPK()));
			processed += loadBy;
			LOGGER.info("Processed {}/{} items for unknown identification", processed, totalItemCount);
		}

		LOGGER.info("Identified {} items not found, in {} events", itemAcumulator.itemCount.size(), itemAcumulator.itemCount.values().stream().map(Counter::getNbEvent).reduce((a, b) -> (a + b)).get());

		HashMap<SPListPK, Long> itemCountByList = new HashMap<>();
		itemAcumulator.itemCount.forEach((pk, c) -> //
		{
			SPListPK listPK = new SPListPK(pk.getId().substring(0, pk.getId().indexOf("/")), pk.getSiteId());
			itemCountByList.put(listPK, itemCountByList.getOrDefault(listPK, 0L) + 1);
		});

		LOGGER.info("Items belongs to {} lists", itemCountByList.size());

		List<Entry<SPListPK, Long>> sortedEntryList = new ArrayList<>(itemCountByList.entrySet());
		sortedEntryList.sort(Entry.comparingByValue());
		Collections.reverse(sortedEntryList);

		int listNotFound = 0;
		int listNotFoundItemCount = 0;
		HashMap<Integer, Long> itemCountByListTemplate = new HashMap<>();

		for (Entry<SPListPK, Long> entry : sortedEntryList) {
			SPList targetList = spListManager.getList(entry.getKey().getSiteId(), entry.getKey().getListId());
			if (targetList == null) {
				listNotFound++;
				listNotFoundItemCount += entry.getValue();
			} else {
				itemCountByListTemplate.put(targetList.getBaseTemplate(), itemCountByListTemplate.getOrDefault(targetList.getBaseTemplate(), 0L) + entry.getValue());
				LOGGER.info("{}: {}/{}", entry.getValue(), targetList.getWebSiteUrl(), entry.getKey().getListId());
			}
		}

		writeCsv(itemAcumulator);

		LOGGER.info("Identified {} events item read", itemAcumulator.itemCount.values().stream().map(Counter::getNbEventItemRead).reduce(Long::sum).get());
		LOGGER.info("Identified {} events other type", itemAcumulator.itemCount.values().stream().map(Counter::getNbEventOthers).reduce(Long::sum).get());
		LOGGER.info("Identified {} event item read classic", itemAcumulator.itemCount.values().stream().map(Counter::getNbEventItemReadClassic).reduce(Long::sum).get());
		LOGGER.info("Identified {} events item read MAA", itemAcumulator.itemCount.values().stream().map(Counter::getNbEventItemReadMAA).reduce(Long::sum).get());
		LOGGER.info("---------------------------------");
		LOGGER.info("Identified {} page read", itemAcumulator.getSum(Counter::getNbEventPageReadClassic));
		LOGGER.info("Identified {} document read", itemAcumulator.getSum(Counter::getNbEventDocumentReadClassic));
		LOGGER.info("Identified {} assert read", itemAcumulator.getSum(Counter::getNbEventAssertReadClassic));

		LOGGER.info("--------------------------------");
		LOGGER.info("Template | Unknown item count");
		itemCountByListTemplate.forEach((t, c) -> LOGGER.info("{} | {}", t, c));
		LOGGER.info("{} list where not found, {} items", listNotFound, listNotFoundItemCount);

		itemCountByList.clear();

	}

	private void writeCsv(MyCallback itemAcumulator) {
		// parse events, every day to write csv with event associated to empty item
		WriterCallback writerCallback = new WriterCallback(itemAcumulator.itemCount);
		LocalDate loopDate = ANALYSIS_PERIOD[0];
		while (!loopDate.isAfter(ANALYSIS_PERIOD[1])) {
			LOGGER.info("Processing events for {}", loopDate);
			eventManager.processEventsOnPeriod(writerCallback, new Day(loopDate));
			LOGGER.info("Processed events for {}", loopDate);
			loopDate = loopDate.plusDays(1);
		}
		writerCallback.finish();
	}

	class Counter {
		Long	nbEvent						= 0L;
		Long	nbEventItemRead				= 0L;

		Long	nbEventItemReadClassic		= 0L;
		Long	nbEventPageReadClassic		= 0L;
		Long	nbEventDocumentReadClassic	= 0L;
		Long	nbEventAssertReadClassic	= 0L;

		Long	nbEventItemReadMAA			= 0L;

		Long	nbEventOthers				= 0L;

		public Long getNbEvent() {
			return nbEvent;
		}

		public Long getNbEventItemRead() {
			return nbEventItemRead;
		}

		public Long getNbEventItemReadClassic() {
			return nbEventItemReadClassic;
		}

		public Long getNbEventPageReadClassic() {
			return nbEventPageReadClassic;
		}

		public Long getNbEventDocumentReadClassic() {
			return nbEventDocumentReadClassic;
		}

		public Long getNbEventAssertReadClassic() {
			return nbEventAssertReadClassic;
		}

		public Long getNbEventItemReadMAA() {
			return nbEventItemReadMAA;
		}

		public Long getNbEventOthers() {
			return nbEventOthers;
		}

	}

	private class WriterCallback implements IEventCallBack {

		private HashMap<SPItemPK, Counter>	itemCount;

		private CsvWriter					csvWriter;

		public WriterCallback(HashMap<SPItemPK, Counter> itemCount) {
			this.itemCount = itemCount;
			csvWriter = new CsvWriter(getCsvFile(), ';', StandardCharsets.UTF_8);

			List<String> headers = new ArrayList<>();
			headers.add("eventId");
			headers.add("listeTmpl");
			headers.add("collectionId");
			headers.add("webSiteId");
			headers.add("listId");
			headers.add("itemId");
			headers.add("eventProperties");
			try {
				csvWriter.writeRecord(headers.toArray(new String[0]));
			} catch (IOException e) {
				LOGGER.error("", e);
			}
		}

		@Override
		public void processObject(Event event) {
			if (filterEvent(event)) {
				SPItemPK itemPk = createItemPK(event);
				if (itemCount.containsKey(itemPk)) {
					try {
						List<String> record = new ArrayList<>();
						record.add(event.getId());
						record.add(event.getPropertyValue(SPEventPropertyNames.LIST_TEMPLATE));
						record.add(event.getPropertyValue(SPEventPropertyNames.COLLECTION_ID));
						record.add(event.getPropertyValue(SPEventPropertyNames.SITE_ID));
						record.add(event.getPropertyValue(SPEventPropertyNames.LIST_ID));
						record.add(event.getPropertyValue(SPEventPropertyNames.ITEM_ID));
						record.add(event.getProperties().toString());
						this.csvWriter.writeRecord(record.toArray(new String[0]));
					} catch (IOException e) {
						LOGGER.error("", e);
					}
				}
			}

		}

		public void finish() {
			if (csvWriter != null) {
				csvWriter.flush();
				csvWriter.close();
			}
		}

	}

	private class MyCallback implements IEventCallBack {

		private HashMap<SPItemPK, Counter> itemCount = new HashMap<>();

		@Override
		public void processObject(Event event) {
			if (filterEvent(event)) {
				SPItemPK itemPk = createItemPK(event);
				Counter counter = itemCount.getOrDefault(itemPk, new Counter());
				counter.nbEvent++;
				if (event.getEventType() == SPEventType.ITEM_READ) {
					counter.nbEventItemRead++;
					if (event.getId().contains("FileAccessed") || event.getId().contains("PageViewed")) {
						counter.nbEventItemReadMAA++;
					} else {
						if (event.getId().startsWith("page-read")) {
							counter.nbEventPageReadClassic++;
						} else if (event.getId().startsWith("document-read")) {
							counter.nbEventDocumentReadClassic++;
						} else if (event.getId().startsWith("asset-read")) {
							counter.nbEventAssertReadClassic++;
						}
						counter.nbEventItemReadClassic++;
					}
				} else {
					counter.nbEventOthers++;
				}
				itemCount.put(itemPk, counter);
			}
		}

		public Long getSum(Function<Counter, Long> mapper) {
			return this.itemCount.values().stream().map(mapper).reduce(Long::sum).get();
		}
	}

	private boolean filterEvent(Event event) {
		// on ne fait des likes que sur des newsfeed, qu'on ne track pas ici, donc je ne prends pas les likes
		switch (event.getEventType()) {
		case SPEventType.ITEM_CREATE:
		case SPEventType.ITEM_READ:
		case SPEventType.ITEM_UPDATE:
			// check if item is stored locally
			Integer template = event.getPropertyValueAsInteger(SPEventPropertyNames.LIST_TEMPLATE);
			if (template == null) {
				return false;
			}
			return spActionCategoryManager.doSaveItem(template);
		default:
			return false;
		}
	}

	public static void main(String[] args) {
		new ITemNotFoundInEventRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

	public String getCsvFile() {
		String resourceURI = RESSOURCE_FOLDER + "/" + NETWORK_ID + "_itemNotFound_" + LocalDate.now() + ".csv";
		//Path filePath = Files.createFile(Paths.get(resourceURI));
		return resourceURI;
	}

	private SPItemPK createItemPK(Event consolidatedEvent) {
		String id = consolidatedEvent.getPropertyValue(SPEventPropertyNames.LIST_ID) + "/" + consolidatedEvent.getPropertyValue(SPEventPropertyNames.ITEM_ID);
		String siteId = new SPSitePK(consolidatedEvent.getPropertyValue(SPEventPropertyNames.COLLECTION_ID), consolidatedEvent.getPropertyValue(SPEventPropertyNames.SITE_ID)).getCombinedSiteId();

		return new SPItemPK(id, siteId);
	}
}
