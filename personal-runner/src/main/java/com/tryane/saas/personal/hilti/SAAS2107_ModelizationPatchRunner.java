package com.tryane.saas.personal.hilti;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.collaborator.CollaboratorProfilePropertyNames;
import com.tryane.saas.core.collaborator.profile.CollaboratorProfileProperties;
import com.tryane.saas.core.collaborator.profile.ICollaboratorProfilePropertiesCrud;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;
import com.tryane.saas.utils.hibernate.ICallBack;
import com.tryane.saas.utils.hibernate.QueryParameter;
import com.tryane.saas.utils.hibernate.ScrollableResultsProcessor;
import com.tryane.saas.utils.hibernate.SingleTransactionHibernateBatch;
import com.tryane.saas.utils.joda.JodaUtils;

public class SAAS2107_ModelizationPatchRunner {

	private static final Logger					LOGGER				= LoggerFactory.getLogger(SAAS2107_ModelizationPatchRunner.class);

	public static final String					NETWORK_ID			= "20212";

	public static final String					MODELIZATION_NAME	= "o365_country#o365_officelocation";

	public static final LocalDate				DATE				= LocalDate.parse("2017-04-16");

	@Autowired
	private INetworkManager						networkManager;

	@Autowired
	private ICollaboratorProfilePropertiesCrud	collaboratorProfilePropertiesCrud;

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, PersonalDatabaseConfig.class);

			SAAS2107_ModelizationPatchRunner runner = new SAAS2107_ModelizationPatchRunner();
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);

			Network currentNetwork = runner.networkManager.getNetworkById(NETWORK_ID);
			ClientContextHolder.setNetwork(currentNetwork);

			Set<String> propertyKeys = CollaboratorProfilePropertyNames.getPropertyFromModelizationName(MODELIZATION_NAME);

			LOGGER.info("Starting collaborator properties update for property {} and date {}", MODELIZATION_NAME, DATE);
			long startTime = System.currentTimeMillis();

			AtomicLong collabPropertiesCount = new AtomicLong();
			SingleTransactionHibernateBatch<CollaboratorProfileProperties> collabpropBatch = runner.collaboratorProfilePropertiesCrud.createCollaboratorProfilePropertiesBatch();

			Map<Long, CollaboratorProfileProperties> collabPropertiesByCollaboratorId = new HashMap<>();
			ScrollableResultsProcessor<CollaboratorProfileProperties> scrollableResultsProcessor = runner.collaboratorProfilePropertiesCrud.createCollaboratorProfilePropertiesScrollableResultsProcessor();
			scrollableResultsProcessor.createQuery("Select p from CollaboratorProfileProperties p, Collaborator c where p.weekDate=:weekDate and p.collaboratorId=c.id and c.external IS FALSE", new QueryParameter("weekDate", JodaUtils.getWeekIdentifier(DATE)));
			scrollableResultsProcessor.processObjects(new ICallBack<CollaboratorProfileProperties>() {
				@Override
				public void processObject(CollaboratorProfileProperties objectToProcess) {
					collabPropertiesByCollaboratorId.put(objectToProcess.getCollaboratorId(), objectToProcess);
				}
			});

			ScrollableResultsProcessor<CollaboratorProfileProperties> scrollableResultsProcessorOldProperties = runner.collaboratorProfilePropertiesCrud.createCollaboratorProfilePropertiesScrollableResultsProcessor();
			scrollableResultsProcessorOldProperties.createQuery("Select p from CollaboratorProfileProperties p, Collaborator c where p.weekDate = :weekDate and p.collaboratorId=c.id and c.external IS FALSE", new QueryParameter("weekDate", JodaUtils.getWeekIdentifier(DATE.minusWeeks(1))));
			scrollableResultsProcessorOldProperties.processObjects(new ICallBack<CollaboratorProfileProperties>() {

				@Override
				public void processObject(CollaboratorProfileProperties currentProperties) {
					CollaboratorProfileProperties referenceProperties = collabPropertiesByCollaboratorId.get(currentProperties.getCollaboratorId());
					if (referenceProperties == null) {
						// probably a deleted collab, no need to update properties 
						return;
					}
					for (String propertyKey : propertyKeys) {
						String oldValue = currentProperties.getPropertyValue(propertyKey);
						// on écrase que si la valeur n'existe pas, pour ne pas perdre l'historique
						if (oldValue == null) {
							currentProperties.setPropertyValue(propertyKey, referenceProperties.getPropertyValue(propertyKey));
							collabpropBatch.addToUpdate(currentProperties);

						}
					}
					collabPropertiesCount.incrementAndGet();
				}

			});

			collabpropBatch.flushAll();

			LOGGER.info("End of  collaborator properties update process. {} collab properties processed. Duration : {}", collabPropertiesCount.get(), DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - startTime));
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}
}
