package com.tryane.saas.personal;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.sp.list.ISPListManager;
import com.tryane.saas.core.sp.list.SPList;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class TestSimpleRunner extends AbstractSpringRunner {

	private final String	NETWORK_ID	= "s443708";

	@Autowired
	private INetworkManager	networkManager;

	@Autowired
	private ISPListManager	listManager;

	@Override
	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));

		SPList list = listManager.getList("47ce6257-57cb-40fb-adf8-bc884f0f9c25/65a2dc63-057a-4e3d-afc2-96033d55c74a", "2f18658d-f137-4d0a-870f-98cf82c5acff");
		listManager.deleteList(list, LocalDate.now());
	}

	public static void main(String[] args) {
		new TestSimpleRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

}
