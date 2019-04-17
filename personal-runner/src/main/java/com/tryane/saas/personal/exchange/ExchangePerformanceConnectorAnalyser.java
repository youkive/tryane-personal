package com.tryane.saas.personal.exchange;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.tryane.saas.core.connector.stats.ConnectorStats;
import com.tryane.saas.core.connector.stats.IConnectorStatsManager;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class ExchangePerformanceConnectorAnalyser extends AbstractSpringRunner {

	public static final String		NETWORK_ID	= "e1";

	public final static Logger		LOGGER		= LoggerFactory.getLogger(ExchangePerformanceConnectorAnalyser.class);

	@Autowired
	private IConnectorStatsManager	connectorStatsManager;

	@Override
	protected void testImplementation() {
		ConnectorStats stats = connectorStatsManager.getLastConnectorStatsForNetwork(NETWORK_ID);

		AtomicLong getMessagesTimeSpent = new AtomicLong(0);
		AtomicLong nbCollab = new AtomicLong();

		JsonNode statsData = stats.getData();
		statsData.fieldNames().forEachRemaining(fieldName -> {
			if (fieldName.contains("tryaneexchange2013")) {
				nbCollab.getAndIncrement();

				JsonNode dataCollab = statsData.get(fieldName);
				getMessagesTimeSpent.addAndGet(dataCollab.get("getMessages").asLong());
			}
		});

		LOGGER.info("time spent getMessages : {}, time spent average : {}", getMessagesTimeSpent.get(), getMessagesTimeSpent.get() / nbCollab.get());
	}

	public static void main(String[] args) {
		new ExchangePerformanceConnectorAnalyser().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

}
