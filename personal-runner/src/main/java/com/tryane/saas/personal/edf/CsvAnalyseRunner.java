package com.tryane.saas.personal.edf;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.csvreader.CsvReader;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;

public class CsvAnalyseRunner extends AbstractSpringRunner {

	private static final Logger	LOGGER			= LoggerFactory.getLogger(CsvAnalyseRunner.class);

	private String				CSV_RESOURCE	= "C:\\Users\\Bastien\\oneDrive_tryane\\OneDrive - TRYANE\\tryane\\notes\\taches_helpdesk\\TA-474 EDF Neo Skype collaborator incohérence\\export_cor_collaborator_readable.csv";

	@Override
	protected void testImplementation() {
		Resource csvResource = new PathMatchingResourcePatternResolver().getResource("file:" + CSV_RESOURCE);
		Long countDeletedAtStart = 0L;

		try (FileInputStream fileInputStream = new FileInputStream(csvResource.getFile())) {
			BOMInputStream bomInputStream = new BOMInputStream(fileInputStream);
			CsvReader csvReader = new CsvReader(bomInputStream, ';', Charsets.UTF_8);

			csvReader.readHeaders();
			while (csvReader.readRecord()) {
				String collaboratorid = csvReader.get("collaboratorid");
				String externalid = csvReader.get("externalid");
				String mailaddress = csvReader.get("mailaddress");
				String creationdate = csvReader.get("creationdate");
				String deletiondate = csvReader.get("deletiondate");

				if (StringUtils.isNotEmpty(creationdate)) {
					LocalDate parseTime = LocalDate.parse(creationdate);
					if (parseTime.isEqual(LocalDate.parse("2017-01-01"))) {
						countDeletedAtStart++;
					}
				}
			}
		} catch (IOException e) {
			LOGGER.error("", e);
		}

		LOGGER.info("{} deleted at migration", countDeletedAtStart);
	}

	public static void main(String[] args) {
		new CsvAnalyseRunner().runTest("dev", PersonalAppConfig.class);
	}
}
