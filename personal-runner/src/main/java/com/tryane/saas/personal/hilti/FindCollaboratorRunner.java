package com.tryane.saas.personal.hilti;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.collaborator.Collaborator;
import com.tryane.saas.core.collaborator.ICollaboratorManager;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;
import com.tryane.saas.utils.hibernate.ICallBack;

public class FindCollaboratorRunner {

	private final static Logger		LOGGER			= LoggerFactory.getLogger(FindCollaboratorRunner.class);

	private static final String		NETWORK_ID		= "20212";

	private static final String		EMAIL_TO_FOUND	= "Younglim.Yang@hilti.com";

	@Autowired
	private INetworkManager			networkManager;

	@Autowired
	private ICollaboratorManager	collaboratorManager;

	public static void main(String[] args) {

		FindCollaboratorRunner runner = new FindCollaboratorRunner();
		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, PersonalDatabaseConfig.class);
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);

			Network currentNetwork = runner.networkManager.getNetworkById(NETWORK_ID);
			ClientContextHolder.setNetwork(currentNetwork);

			List<Collaborator> collaboratorsFound = new ArrayList<>();
			runner.collaboratorManager.processAllCollaboratorsForClient(new ICallBack<Collaborator>() {

				@Override
				public void processObject(Collaborator collab) {
					if (collab.getMailAddress().toLowerCase().equals(EMAIL_TO_FOUND.toLowerCase())) {
						collaboratorsFound.add(collab);
					}
				}
			});

			LOGGER.info("Found collaborators : {}", collaboratorsFound.size());
			for (Collaborator collab : collaboratorsFound) {
				LOGGER.info("{} : {} : {}(deletiondate) : {}(creationdate) : {}(isExternal) : {}(YammerId)", collab.getId(), collab.getMailAddress(), collab.getDeletionDate(), collab.getCreationDate(), collab.isExternal(), collab.getExternalId());
			}

		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}

	}

}
