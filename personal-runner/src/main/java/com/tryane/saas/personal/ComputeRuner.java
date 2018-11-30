package com.tryane.saas.personal;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;
import com.tryane.saas.results.IResultProcessor;

public class ComputeRuner extends AbstractSpringRunner {

	private static final String		NETWORK_ID		= "s1";

	private static final LocalDate	COMPUTE_DATE	= LocalDate.parse("2018-11-25");

	@Autowired
	private INetworkManager			networkManager;

	@Autowired
	private IResultProcessor		resultProcessor;

	@Override
	protected void testImplementation() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));
		resultProcessor.computeResults(COMPUTE_DATE);
	}

	public static void main(String[] args) {
		new ComputeRuner().runTest("", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}
}
