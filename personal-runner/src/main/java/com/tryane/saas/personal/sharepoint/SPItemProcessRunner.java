package com.tryane.saas.personal.sharepoint;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.core.sp.item.ISPItemManager;
import com.tryane.saas.core.sp.item.SPItem;
import com.tryane.saas.core.sp.site.ISPSiteManager;
import com.tryane.saas.core.sp.site.SPSite;
import com.tryane.saas.core.sp.site.SPSitePK;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;
import com.tryane.saas.personal.sharepoint.manager.IPersonalSPItemManager;
import com.tryane.saas.utils.hibernate.ICallBack;
import com.tryane.saas.utils.string.StringUtils;

public class SPItemProcessRunner {

	private static final Logger		LOGGER	= LoggerFactory.getLogger(SPItemProcessRunner.class);

	@Autowired
	private INetworkManager			networkManager;

	@Autowired
	private IPersonalSPItemManager	personalItemManager;

	@Autowired
	private ISPSiteManager			siteManager;

	@Autowired
	private ISPItemManager			itemManager;

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "dev");
		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, PersonalDatabaseConfig.class);
			SPItemProcessRunner runner = new SPItemProcessRunner();
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);
			runner.execute();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	private final String NETWORK_ID = "s1452";

	public void execute() {
		Network network = networkManager.getNetworkById(NETWORK_ID);
		ClientContextHolder.setNetwork(network);

		displayItemWithFileUniqId("4c05759e-aa82-4319-858d-8c469fa1c5da");
	}

	public void searchItem() {
		Set<String> siteIds = new HashSet<>();

		personalItemManager.processAllItems(new ICallBack<SPItem>() {

			@Override
			public void processObject(SPItem item) {
				if (StringUtils.isNotNullNorEmpty(item.getName()) && item.getName().toLowerCase().contains("cocktail")) {
					LOGGER.info(item.getSpItemPK().getSiteId() + "/" + item.getSpItemPK().getId());
					siteIds.add(item.getSiteId());
				}
			}
		});

		LOGGER.info("");
		siteIds.forEach(siteId -> {
			SPSite webSite = siteManager.getSPSiteById(new SPSitePK(siteId));
			LOGGER.info("{} ({})", webSite.getUrl(), webSite.getCombinedSiteId());
		});
	}

	public void displayItemWithFileUniqId(String fileUniqId) {
		List<SPItem> items = itemManager.getItemsByFileUniqId(fileUniqId);
		items.forEach(item -> {
			LOGGER.info("{}/{}", item.getSpItemPK().getSiteId(), item.getId());
			LOGGER.info("fileUniqId : {}", item.getFileUniqId());
			LOGGER.info("name : {}", item.getName());
			LOGGER.info("data : {}", item.getData());
		});
	}
}
