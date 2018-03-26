package com.tryane.saas.personal.sharepoint;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.core.sp.item.ISPItemManager;
import com.tryane.saas.core.sp.item.SPItem;
import com.tryane.saas.core.sp.item.SPItemPropertiesName;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;
import com.tryane.saas.utils.hibernate.ICallBack;
import com.tryane.saas.utils.string.StringUtils;

public class SPItemProcessRunner {

	private static final Logger	LOGGER	= LoggerFactory.getLogger(SPItemProcessRunner.class);

	@Autowired
	private INetworkManager		networkManager;

	@Autowired
	private ISPItemManager		itemManager;

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

	private final String NETWORK_ID = "s443632";

	public void execute() {
		Network network = networkManager.getNetworkById(NETWORK_ID);
		ClientContextHolder.setNetwork(network);

		AtomicLong countItemWithoutcreatorId = new AtomicLong(0);
		itemManager.processAllItems(new ICallBack<SPItem>() {

			@Override
			public void processObject(SPItem item) {
				String creatorId = item.getDataValue(SPItemPropertiesName.CREATOR_ITEM_EXTERNAL_ID);
				if (StringUtils.isNullOrEmpty(creatorId)) {
					countItemWithoutcreatorId.incrementAndGet();
				}
			}
		});

		LOGGER.info("found {} items without creatorId", countItemWithoutcreatorId.get());
	}
}
