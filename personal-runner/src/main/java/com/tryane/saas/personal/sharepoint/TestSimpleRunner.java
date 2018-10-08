package com.tryane.saas.personal.sharepoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.connector.sharepoint.items.ISPItemUtils;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.sp.list.ISPListManager;
import com.tryane.saas.core.sp.list.SPList;
import com.tryane.saas.core.sp.list.SPListPropertiesNames;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class TestSimpleRunner extends AbstractSpringRunner {

	private static final Logger	LOGGER		= LoggerFactory.getLogger(TestSimpleRunner.class);

	private static final String	NETWORK_ID	= "s443673";

	@Autowired
	private INetworkManager		networkManager;

	@Autowired
	private ISPListManager		listManager;

	@Autowired
	private ISPItemUtils		spItemUtils;

	@Override
	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));

		SPList list = listManager.getList("89feb1d9-ea7d-4aed-8427-022d908d8542/8f2227a3-20d8-42ba-a91e-59f11b83bc55", "f6ce2fb5-6efa-4ad9-8c73-aacb728d5092");
		if (spItemUtils.isTargetListToSaveItem(list)) {
			LOGGER.info("arrrf");
		} else {
			LOGGER.info("ok");
		}
		String debug = list.getData().path(SPListPropertiesNames.DOCUMENT_TEMPLATE_URL).asText();
		LOGGER.info("{}", list.getData().path(SPListPropertiesNames.DOCUMENT_TEMPLATE_URL).asText());
	}

	public static void main(String[] args) {
		new TestSimpleRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

}
