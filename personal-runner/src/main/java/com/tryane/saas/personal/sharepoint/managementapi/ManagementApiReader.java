package com.tryane.saas.personal.sharepoint.managementapi;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvReader;
import com.tryane.saas.connector.managmentapi.model.O365ManagmentContent;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

import nl.basjes.shaded.com.google.common.collect.Lists;

public class ManagementApiReader extends AbstractSpringRunner {

	private static final Logger	LOGGER				= LoggerFactory.getLogger(ManagementApiReader.class);

	private static final String	RESSOURCE_FOLDER	= "src/main/resources/com/tryane/saas/personal/sharepoint/managmentapi";

	private static final String	FILE_NAME			= "s443571_2018-09-19.csv";

	public static void main(String[] args) {
		new ManagementApiReader().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

	@Override
	protected void testImplementation() {
		CsvReader reader = null;
		List<O365ManagmentContent> contents = Lists.newArrayList();
		try {
			reader = new CsvReader(RESSOURCE_FOLDER + "/" + FILE_NAME, ';');
			reader.readHeaders();
			while (reader.readRecord()) {
				contents.add(mapToContent(reader));
			}

			Long count = contents.stream().filter(content -> "1FDACD9A-3633-4AEB-AF79-5D5E4C411458".toLowerCase().equals(content.getListItemUniqId())).count();

			contents.stream().filter(content -> "1FDACD9A-3633-4AEB-AF79-5D5E4C411458".toLowerCase().equals(content.getListItemUniqId())).forEach(content -> {
				LOGGER.info("{} | {}", content.getCreationTime(), content.getOperation());
			});

			LOGGER.info("find {} items for doucment", count);
		} catch (IOException e) {
			LOGGER.error("", e);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	private O365ManagmentContent mapToContent(CsvReader csvReader) throws IOException {
		O365ManagmentContent content = new O365ManagmentContent();
		content.setListItemUniqId(csvReader.get("ListItemUniqId"));
		content.setOperation(csvReader.get("Operation"));
		content.setCreationTime(csvReader.get("CreationTime"));
		return content;
	}
}
