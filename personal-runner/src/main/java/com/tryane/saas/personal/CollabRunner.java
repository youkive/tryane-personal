package com.tryane.saas.personal;

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

/**
 * Parcourir les collaborateurs d'un network pour récupéré certaines infos, avec filtres
 */
public class CollabRunner {

	private static Logger			LOGGER				= LoggerFactory.getLogger(CollabRunner.class);

	private static final String		NETWORK_ID			= "s443696";

	private static final String		EMAIL_FILTER		= "katelyn.estelow@avantorsciences.com";

	private static final String		EXTERNAL_ID_FILTER	= "i:0#.f|membership|johanna.goslin@avantorsciences.com";

	@Autowired
	private INetworkManager			networkManager;

	@Autowired
	private ICollaboratorManager	collaboratorManager;

	public static void main(String[] args) {

		CollabRunner runner = new CollabRunner();
		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, PersonalDatabaseConfig.class);
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);

			runner.execute();

		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	public void execute() {
		Network bouyguesNetwork = networkManager.getNetworkById(NETWORK_ID);
		ClientContextHolder.setNetwork(bouyguesNetwork);

		//searchCollaboratorByEmail(EMAIL_FILTER);
		searchCollaboratorByExternalId(EXTERNAL_ID_FILTER);
		//displayAllCollaborators();
		//displayCollab(1049L);
	}

	private void displayAllCollaborators() {
		collaboratorManager.processAllCollaboratorsForClient(new ICallBack<Collaborator>() {

			@Override
			public void processObject(Collaborator collaborator) {
				if (!collaborator.getExternalId().contains("@hilti.com")) {
					LOGGER.info("{}", collaborator.getExternalId());
				}
			}
		});
	}

	private void searchCollaboratorByExternalId(String externalIdToSearch) {
		collaboratorManager.processAllCollaboratorsForClient(new ICallBack<Collaborator>() {

			@Override
			public void processObject(Collaborator collaborator) {
				if (collaborator.getExternalId().equals(externalIdToSearch)) {
					displayCollab(collaborator.getId());
				}
			}
		});
	}

	private void searchCollaboratorById(Long collaboratorId) {
		displayCollab(collaboratorId);
	}

	private void searchCollaboratorByEmail(String emailSearched) {
		collaboratorManager.processAllCollaboratorsForClient(new ICallBack<Collaborator>() {

			@Override
			public void processObject(Collaborator collaborator) {
				if (collaborator.getMailAddress().equals(emailSearched)) {
					displayCollab(collaborator.getId());
				}
			}
		});
	}

	private void displayCollab(Long collaboratorId) {
		Collaborator collab = collaboratorManager.getCollaboratorById(collaboratorId);

		LOGGER.info("Collab Id : {}", collab.getId());
		LOGGER.info("Collab by email : {}", collab.getMailAddress());
		LOGGER.info("External ID : {}", collab.getExternalId());
		LOGGER.info("Deletion date : {}", collab.getDeletionDate());
		LOGGER.info("Collab by props : {}", collab.getProps().toString());
	}
}
