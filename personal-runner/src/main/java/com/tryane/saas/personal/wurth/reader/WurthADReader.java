package com.tryane.saas.personal.wurth.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvReader;
import com.tryane.saas.connector.graph.utils.api.reports.reader.GraphReportReader;
import com.tryane.saas.personal.wurth.item.WurthADUser;

public class WurthADReader extends GraphReportReader<WurthADUser> {

	private final String	ID_HEADER		= "cn";

	private final String	EMAIL_HEADER	= "Workemail";

	public WurthADReader(File csvFile) throws IOException {
		super(csvFile);
		csvReader = new CsvReader(new FileInputStream(csvFile), ';', StandardCharsets.UTF_8);
		csvReader.readHeaders();
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(WurthADReader.class);

	@Override
	public WurthADUser readNextLine() throws IOException {
		if (!csvReader.readRecord()) {
			return null;
		}

		WurthADUser item = new WurthADUser();
		item.setId(csvReader.get(ID_HEADER));
		item.setEmail(csvReader.get(EMAIL_HEADER));
		return item;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

}
