package com.tryane.saas.personal.wurth.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvReader;
import com.tryane.saas.connector.graph.utils.api.reports.reader.GraphReportReader;
import com.tryane.saas.personal.wurth.item.TryaneExportUser;

public class TryaneExportReader extends GraphReportReader<TryaneExportUser> {

	private final String	SP_ID	= "SharePoint Id";

	private final String	EMAIL	= "Nom ou adresse email";

	public TryaneExportReader(File csvFile) throws IOException {
		super(csvFile);
		csvReader = new CsvReader(new FileInputStream(csvFile), ';', StandardCharsets.UTF_8);
		csvReader.readHeaders();
	}

	private final Logger LOGGER = LoggerFactory.getLogger(TryaneExportReader.class);

	@Override
	public TryaneExportUser readNextLine() throws IOException {
		if (!csvReader.readRecord()) {
			return null;
		}

		TryaneExportUser item = new TryaneExportUser();
		item.setSpId(csvReader.get(SP_ID));
		item.setEmail(csvReader.get(EMAIL));
		return item;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

}
