package com.tryane.saas.personal.sharepoint.missingitem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.csvreader.CsvReader;
import com.google.common.base.Joiner;
import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.connector.o365.utils.exception.O365UserAuthenticationException;
import com.tryane.saas.connector.o365.utils.token.IAppTokenManager;
import com.tryane.saas.connector.sharepoint.utils.api.ISPWebListAPI;
import com.tryane.saas.connector.sharepoint.utils.model.SharepointSPItem;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.event.Event;
import com.tryane.saas.core.event.IEventManager;
import com.tryane.saas.core.event.props.SPEventPropertyNames;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.core.sp.item.ISPItemManager;
import com.tryane.saas.core.sp.item.SPItem;
import com.tryane.saas.core.sp.item.SPItemPK;
import com.tryane.saas.core.sp.list.ISPListManager;
import com.tryane.saas.core.sp.list.SPList;
import com.tryane.saas.core.sp.site.ISPSiteManager;
import com.tryane.saas.core.sp.site.SPSite;
import com.tryane.saas.core.sp.site.SPSitePK;
import com.tryane.saas.core.sp.sitecol.ISPSiteCollectionManager;
import com.tryane.saas.core.sp.sitecol.SPSiteCollection;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;
import com.tryane.saas.utils.hibernate.ICallBack;

public class ItemEventAnalyseRunner extends AbstractSpringRunner {

	private static final Logger			LOGGER			= LoggerFactory.getLogger(ItemEventAnalyseRunner.class);

	private static final String			CSV_DIRECTORY	= "src/main/resources/com/tryane/saas/personal/sharepoint/missingitems";

	private static final String			FILE_NAME		= "s443673_itemNotFound_2018-10-02.csv";

	private static final String			NETWORK_ID		= "s443673";

	private static final String			EVENT_ID		= "document-read-href&6720&2018-09-28T16:22:51.406Z&c4d10712-6351-49f6-9585-f2f825b54acf";

	@Autowired
	private INetworkManager				networkManager;

	@Autowired
	private INetworkPropertyManager		networkPropertyManager;

	@Autowired
	private IEventManager				eventManager;

	@Autowired
	private ISPSiteCollectionManager	siteCollectionManager;

	@Autowired
	private ISPSiteManager				siteManager;

	@Autowired
	private ISPListManager				listManager;

	@Autowired
	private ISPItemManager				itemManager;

	@Autowired
	private IAppTokenManager			appTokenManager;

	@Autowired
	private ISPWebListAPI				listApi;

	private String						tenantId;

	private String						mainCollectionUrl;

	public static void main(String[] args) {
		new ItemEventAnalyseRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

	@Override
	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));
		tenantId = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_TENANT);
		mainCollectionUrl = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_MAINCOLLECTION_URL);
		appTokenManager.initForTenant(tenantId);

		readCsv();

		appTokenManager.clearForTenant(tenantId);
	}

	public void analyze() {
		Event event = eventManager.getEventById(EVENT_ID);
		SPSiteCollection siteCollection = siteCollectionManager.getSPSiteCollectionById(event.getPropertyValue(SPEventPropertyNames.COLLECTION_ID));
		SPSite website = siteManager.getSPSiteById(new SPSitePK(siteCollection.getId(), event.getPropertyValue(SPEventPropertyNames.SITE_ID)));
		SPList list = listManager.getList(new SPSitePK(website), event.getPropertyValue(SPEventPropertyNames.LIST_ID));
		String itemId = event.getPropertyValue(SPEventPropertyNames.ITEM_ID);

		LOGGER.info("Collection {} | {}", siteCollection.getUrl(), siteCollection.getId());
		LOGGER.info("Website {} | {}", website.getUrl(), website.getId());
		LOGGER.info("List template {}", list.getBaseTemplate());

		SPItem item = itemManager.getItem(list.getSpListPK().getListId(), itemId, website.getSitePK());
		if (item == null) {
			LOGGER.info("Item not found in db");
			try {
				SharepointSPItem itemOnSP = listApi.getItemInList(website.getUrl(), appTokenManager.geAppTokenGenerator(mainCollectionUrl, tenantId).getToken(), list.getSpListPK().getListId(), itemId);
				LOGGER.info("name : {}", itemOnSP.getTitle());
			} catch (O365ConnectionException | O365HttpErrorException | O365UserAuthenticationException e) {
				LOGGER.error("", e);
			}
		} else {
			LOGGER.info("Item found in db");
		}
	}

	public void readCsv() {
		AnalyseRecordCallback callback = new AnalyseRecordCallback();
		CsvReader reader = null;
		try {
			reader = new CsvReader(CSV_DIRECTORY + "/" + FILE_NAME, ';');
			reader.readHeaders();
			while (reader.readRecord()) {
				CsvRecord record = new CsvRecord().build(reader);
				callback.processObject(record);
			}
		} catch (FileNotFoundException e) {
			LOGGER.error("", e);
		} catch (IOException e) {
			LOGGER.error("", e);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

		callback.searchItemsOnSP();
		LOGGER.info("{}", Joiner.on(";").join(callback.itemDateFoundOnSP).toString());

		LOGGER.info("");
		callback.itemsPkFoundOnSP.stream().map(itemPK -> itemPK.getSiteId() + "/" + itemPK.getId().split("/")[0]).distinct().sorted().forEach(listId -> LOGGER.info("{}", listId));

		LOGGER.info("Find {} uniq itemPK", callback.itemPksFind.size());
		LOGGER.info("Find {} items On SP", callback.itemsFoundOnSPCount);
	}

	class CsvRecord {
		public String	eventId;

		public String	collectionId;

		public String	websiteId;

		public String	listId;

		public String	itemId;

		public CsvRecord build(CsvReader reader) throws IOException {
			this.eventId = reader.get("eventId");
			this.collectionId = reader.get("collectionId");
			this.websiteId = reader.get("webSiteId");
			this.listId = reader.get("listId");
			this.itemId = reader.get("itemId");
			return this;
		}

	}

	class AnalyseRecordCallback implements ICallBack<CsvRecord> {

		Set<SPItemPK>	itemPksFind			= new HashSet<>();

		Long			itemsFoundOnSPCount	= 0L;

		Set<SPItemPK>	itemsPkFoundOnSP	= new HashSet<>();

		Set<LocalDate>	itemDateFoundOnSP	= new HashSet<>();

		@Override
		public void processObject(CsvRecord record) {
			if (!record.eventId.startsWith("page-read")) {
				return;
			}

			itemPksFind.add(builItemPKForRecord(record));
		}

		public void searchItemsOnSP() {
			for (SPItemPK itemPk : itemPksFind) {
				String listId = itemPk.getId().split("/")[0];
				String itemId = itemPk.getId().split("/")[1];

				SPSite website = siteManager.getSPSiteById(new SPSitePK(itemPk.getSiteId()));
				try {
					SharepointSPItem itemSP = listApi.getItemInList(website.getUrl(), appTokenManager.geAppTokenGenerator(mainCollectionUrl, tenantId).getToken(), listId, itemId);
					if (itemSP != null) {
						itemsFoundOnSPCount++;
						LocalDate creationDate = LocalDateTime.parse(itemSP.getCreatedAt(), ISODateTimeFormat.dateTimeParser()).toLocalDate();
						itemDateFoundOnSP.add(creationDate);
						itemsPkFoundOnSP.add(itemPk);
					}
				} catch (O365ConnectionException | O365HttpErrorException | O365UserAuthenticationException e) {
					LOGGER.warn("{}", e.getMessage());
				}
			}
		}

	}

	public SPItemPK builItemPKForRecord(CsvRecord record) {
		SPItemPK itemPk = new SPItemPK();
		itemPk.setId(record.listId + "/" + record.itemId);
		itemPk.setSiteId(record.collectionId + "/" + record.websiteId);
		return itemPk;
	}

}
