package com.tryane.saas.personal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.sp.item.ISPItemManager;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class SearchItemRunner {

	private static final Logger	LOGGER		= LoggerFactory.getLogger(SearchItemRunner.class);

	private final String		NETWORK_ID	= "s443662";

	@Autowired
	private ISPItemManager		itemManager;

	@Autowired
	private INetworkManager		networkManager;

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = null;

		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, PersonalDatabaseConfig.class);

			SearchItemRunner runner = new SearchItemRunner();
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);

			runner.execute();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	public void execute() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));

		itemManager.processAllItems(item -> {
			if (item.getName() != null && item.getName().contains("2009-05-18")) {
				LOGGER.info("BLAH");
			}
		});
	}
}
