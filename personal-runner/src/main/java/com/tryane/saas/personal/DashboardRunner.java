package com.tryane.saas.personal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.user.dashboard.IMyDashboardManager;
import com.tryane.saas.core.user.dashboard.MyDashboard;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class DashboardRunner extends AbstractSpringRunner {

	private static final Logger	LOGGER		= LoggerFactory.getLogger(DashboardRunner.class);

	private static final String	NETWORK_ID	= "s1";

	@Autowired
	private INetworkManager		networkManager;

	@Autowired
	private IMyDashboardManager	dashboardManager;

	@Override
	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));
		dashboardManager.processAllMydashBoard(dashboard -> displayDashboardInfo(dashboard));
	}

	private void displayDashboardInfo(MyDashboard dashboard) {
		LOGGER.info("dashboardId : {}", dashboard.getId());
		LOGGER.info("owner : {}", dashboard.getUserId());
		LOGGER.info("data : {}", dashboard.getData().toString());
	}

	public static void main(String[] args) {
		new DashboardRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

}
