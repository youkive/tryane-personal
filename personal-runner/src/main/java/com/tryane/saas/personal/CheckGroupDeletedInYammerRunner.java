package com.tryane.saas.personal;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.google.common.collect.Lists;
import com.tryane.saas.connector.yammer.api.exception.YammerConnectionException;
import com.tryane.saas.connector.yammer.api.exception.YammerHttpErrorException;
import com.tryane.saas.connector.yammer.api.export.YammerExportAPI;
import com.tryane.saas.connector.yammer.common.model.user.YammerCsvGroup;
import com.tryane.saas.connector.yammer.newapi.IYammerExportAPI;
import com.tryane.saas.connector.yammer.utils.YammerCSVUtils;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.esn.group.Group;
import com.tryane.saas.core.esn.group.IGroupManager;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class CheckGroupDeletedInYammerRunner {

	private static final Logger		LOGGER		= LoggerFactory.getLogger(CheckGroupDeletedInYammerRunner.class);

	private static final String		NETWORK_ID	= "1871154";

	@Autowired
	private INetworkManager			networkManager;

	@Autowired
	private INetworkPropertyManager	networkPropertyManager;

	@Autowired
	private IYammerExportAPI		exportAPI;

	@Autowired
	private IGroupManager			groupManager;

	public static void main(String[] args) throws YammerHttpErrorException, YammerConnectionException {
		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, PersonalDatabaseConfig.class);
			CheckGroupDeletedInYammerRunner runner = new CheckGroupDeletedInYammerRunner();
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);
			runner.execute();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	public void execute() throws YammerHttpErrorException, YammerConnectionException {
		Network network = networkManager.getNetworkById(NETWORK_ID);
		ClientContextHolder.setNetwork(network);

		String token = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.YAMMER_TOKEN);
		File groupCsvExport = exportAPI.getExportZipSinceBegin(token, YammerExportAPI.MODEL_GROUP);

		File groupCsvFile = YammerCSVUtils.getExportCsv(groupCsvExport, YammerExportAPI.GROUP_CSV_NAME);
		List<String[]> groupsLines = YammerCSVUtils.extractFromFile(groupCsvFile);

		if (groupsLines.isEmpty()) {
			LOGGER.warn("Csv groups export is empty");
			//throw new YammerExportException("Csv groups export is empty");
		}

		Integer numberOfGroupNotDeleted = 0;
		Integer numberOfGroupDeletedNotInsertedInDb = 0;
		List<String> groupIdsInError = Lists.newArrayList();
		for (String[] groupLine : groupsLines) {
			YammerCsvGroup yammerCsvGroup = new YammerCsvGroup(groupLine);

			Group groupInBase = groupManager.getGroup(yammerCsvGroup.getId().toString());
			if (groupInBase == null) {
				if (yammerCsvGroup.getDeleted()) {
					numberOfGroupDeletedNotInsertedInDb++;
				}
				continue;
			}
			if (groupInBase.getDeletionDate() == null && yammerCsvGroup.getDeleted()) {
				numberOfGroupNotDeleted++;
				groupIdsInError.add(groupInBase.getGroupId());
			}
		}

		LOGGER.info("Number of group deleted not found in DB : {}", numberOfGroupDeletedNotInsertedInDb);
		LOGGER.info("Number of group in error : {}", numberOfGroupNotDeleted);
	}
}
