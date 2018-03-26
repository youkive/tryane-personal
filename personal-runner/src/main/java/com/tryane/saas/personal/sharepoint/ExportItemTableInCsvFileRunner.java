package com.tryane.saas.personal.sharepoint;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.core.sp.item.ISPItemManager;
import com.tryane.saas.core.sp.item.SPItem;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;
import com.tryane.saas.utils.hibernate.ICallBack;

public class ExportItemTableInCsvFileRunner {

	private static final Logger	LOGGER	= LoggerFactory.getLogger(ExportItemTableInCsvFileRunner.class);

	@Autowired
	private INetworkManager		networkManager;

	@Autowired
	private ISPItemManager		itemManager;

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "dev");
		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, PersonalDatabaseConfig.class);

			ExportItemTableInCsvFileRunner runner = new ExportItemTableInCsvFileRunner();
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);
			try {
				runner.execute();
			} catch (IOException e) {
				LOGGER.error("", e);
			}
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	private final String NETWORK_ID = "s443632";

	public void execute() throws IOException {
		Network network = networkManager.getNetworkById(NETWORK_ID);
		ClientContextHolder.setNetwork(network);

		Path filePath = Paths.get(System.getenv("SAAS_HOME"), "export_sp_item.log");
		try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
			itemManager.processAllItems(new ICallBack<SPItem>() {

				@Override
				public void processObject(SPItem item) {
					List<String> itemsToPrint = Lists.newArrayList(item.getId(), item.getSiteId(), item.getFileUniqId(), item.getData().toString());
					try {
						writer.write(Joiner.on(";").skipNulls().join(itemsToPrint));
						writer.newLine();
					} catch (IOException e) {
						LOGGER.error("", e);
					}
				}
			});
		}
	}
}
