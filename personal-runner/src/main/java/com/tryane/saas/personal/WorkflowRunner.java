package com.tryane.saas.personal;

import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;
import com.tryane.saas.results.workflow.execution.IWorkflowTriggerExecutor;

public class WorkflowRunner extends AbstractSpringRunner {

	private final String				NETWORK_ID	= "t1";

	@Autowired
	private INetworkManager				networkManager;

	@Autowired
	private IWorkflowTriggerExecutor	workflowTriggerExcecutor;

	@Override
	protected void testImplementation() {
		Network network = networkManager.getNetworkById(NETWORK_ID);
		ClientContextHolder.setNetwork(network);

		workflowTriggerExcecutor.executeAllWorkflows();
	}

	public static void main(String[] args) {
		new WorkflowRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

}
