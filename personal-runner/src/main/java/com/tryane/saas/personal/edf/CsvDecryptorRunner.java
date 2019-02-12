package com.tryane.saas.personal.edf;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.utils.crypto.AesCryptoManager;
import com.tryane.saas.utils.crypto.ICryptoManager;

public class CsvDecryptorRunner extends AbstractSpringRunner {

	private static final Logger	LOGGER			= LoggerFactory.getLogger(CsvDecryptorRunner.class);

	private String				CSV_RESOURCE	= "C:\\Users\\Bastien\\oneDrive_tryane\\OneDrive - TRYANE\\tryane\\notes\\taches_helpdesk\\TA-481 EDF Neo Evolution suspecte  incoherence du nombre de collaborateurs SharePoint\\20190121_Export_s1_core_collaborator.csv";

	private String				OUT_RESOURCE	= "src/main/resources/com/tryane/saas/personal/edf/out.csv";

	@Override
	protected void testImplementation() {
		Resource csvResource = new PathMatchingResourcePatternResolver().getResource("file:" + CSV_RESOURCE);

		ICryptoManager cryptomanager = new AesCryptoManager();

		try (FileInputStream fileInputStream = new FileInputStream(csvResource.getFile())) {
			BOMInputStream bomInputStream = new BOMInputStream(fileInputStream);
			CsvReader csvReader = new CsvReader(bomInputStream, ';', Charsets.UTF_8);

			CsvWriter csvWriter = new CsvWriter(OUT_RESOURCE, ';', StandardCharsets.UTF_8);
			csvWriter.writeRecord(new String[] { "collaboratorid", "externalid", "mailaddress", "creationdate", "deletiondate" });

			List<String[]> cache = new ArrayList<>();
			csvReader.readHeaders();
			while (csvReader.readRecord()) {
				String collaboratorid = csvReader.get("collaboratorid");
				String externalid = csvReader.get("externalid");
				String mailaddress = csvReader.get("mailaddress");
				String creationdate = csvReader.get("creationdate");
				String deletiondate = csvReader.get("deletiondate");

				cache.add(new String[] { collaboratorid, decryptToken(externalid, cryptomanager), decryptToken(mailaddress, cryptomanager), creationdate, deletiondate });
				if (flushCache(csvWriter, cache)) {
					cache = new ArrayList<>();
				}
				LOGGER.info("{}, {}", decryptToken(externalid, cryptomanager), decryptToken(mailaddress, cryptomanager));
			}

			flushCache(csvWriter, cache);
			if (csvWriter != null) {
				csvWriter.flush();
				csvWriter.close();
			}
		} catch (IOException e) {
			LOGGER.error("", e);
		}

	}

	private Boolean flushCache(CsvWriter writer, List<String[]> cache) {
		if (cache.size() >= 10000) {
			cache.forEach(record -> {
				try {
					writer.writeRecord(record);
				} catch (IOException e) {
					LOGGER.error("", e);
				}
			});
			writer.flush();
			return true;
		}
		return false;
	}

	public final static String decryptToken(String encryptedToken, ICryptoManager cryptomanager) {
		int len = encryptedToken.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(encryptedToken.charAt(i), 16) << 4) + Character.digit(encryptedToken.charAt(i + 1), 16));
		}
		return cryptomanager.decrypt(data);
	}

	public static void main(String[] args) {
		new CsvDecryptorRunner().runTest("dev", PersonalAppConfig.class);
	}
}
