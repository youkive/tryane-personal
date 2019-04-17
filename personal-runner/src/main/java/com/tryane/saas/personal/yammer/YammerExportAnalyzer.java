package com.tryane.saas.personal.yammer;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvReader;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;

public class YammerExportAnalyzer extends AbstractSpringRunner {

	private static final String	RESSOURCE_FOLDER	= "src/main/resources/com/tryane/saas/personal/yammer/export";

	private static final String	FILE_NAME_CSV		= "yammerExport_renault_messagesModel.csv";

	private static final String	GROUP_ID			= "12441257";

	public static void main(String[] args) {
		new YammerExportAnalyzer().runTest("dev", PersonalAppConfig.class);
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(YammerExportAnalyzer.class);

	@Override
	protected void testImplementation() {
		CsvReader reader = null;
		AtomicLong counter = new AtomicLong(0);
		try {
			reader = new CsvReader(RESSOURCE_FOLDER + "/" + FILE_NAME_CSV, ',');
			reader.readHeaders();
			while (reader.readRecord()) {
				String groupId = reader.get("group_id");
				if (GROUP_ID.equals(groupId)) {
					counter.getAndIncrement();
					LOGGER.info("id : {}", reader.get("id"));
				}
			}
		} catch (IOException e) {
			LOGGER.error("", e);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

		LOGGER.info("counter : {}", counter.get());

	}
}
