package com.tryane.saas.personal.sharepoint;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.connector.o365.utils.exception.O365UserAuthenticationException;
import com.tryane.saas.connector.o365.utils.token.AppTokenManager;
import com.tryane.saas.connector.sharepoint.utils.api.ISPUserProfileAPI;
import com.tryane.saas.connector.sharepoint.utils.api.batch.api.ISPBatchRequestAPI;
import com.tryane.saas.connector.sharepoint.utils.api.batch.api.SPODataBatchResponse;
import com.tryane.saas.connector.sharepoint.utils.model.SPUserProfile;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.collaborator.Collaborator;
import com.tryane.saas.core.collaborator.ICollaboratorManager;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;
import com.tryane.saas.utils.hibernate.ICallBack;

public class SPProfileBatchRunner {

	private static final Logger			LOGGER		= LoggerFactory.getLogger(SPProfileBatchRunner.class);

	private final static String			NETWORK_ID	= "s140";

	@Autowired
	private ISPUserProfileAPI			userProfileAPI;

	@Autowired
	private INetworkManager				networkManager;

	@Autowired
	private ISPBatchRequestAPI<String>	batchAPI;

	@Autowired
	private AppTokenManager				appTokenManager;

	@Autowired
	private INetworkPropertyManager		networkPropertyManager;

	@Autowired
	private ICollaboratorManager		collaboratorManager;

	public static void main(String[] args) {
		//new SPProfileBatchRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
		System.setProperty("spring.profiles.active", "dev");
		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, PersonalDatabaseConfig.class);
			SPProfileBatchRunner runner = new SPProfileBatchRunner();
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);

			runner.testImplementation();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	protected void testImplementation() {
		Network network = networkManager.getNetworkById(NETWORK_ID);
		ClientContextHolder.setNetwork(network);

		String tenantId = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_TENANT);
		String spUrl = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_MAINCOLLECTION_URL);

		appTokenManager.initForTenant(tenantId);
		CollaboratorCallback_Batch callback = new CollaboratorCallback_Batch(tenantId, spUrl);
		collaboratorManager.processAllValidCollaboratorsForClientAtDate(callback, LocalDate.now());
		callback.finish();
	}

	class CollaboratorCallback_Batch implements ICallBack<Collaborator> {

		private String				spUrl;

		private String				tenantId;

		private List<Collaborator>	queue;

		private final Integer		MAX_SIZE	= 50;

		private final AtomicLong	count		= new AtomicLong(0);

		private final AtomicLong	countError	= new AtomicLong(0);

		public CollaboratorCallback_Batch(String tenantId, String spUrl) {
			queue = new ArrayList<>();
			this.tenantId = tenantId;
			this.spUrl = spUrl;
		}

		@Override
		public void processObject(Collaborator collaborator) {
			queue.add(collaborator);
			if (queue.size() == MAX_SIZE) {
				flush();
				queue = new ArrayList<>();
			}
		}

		public void finish() {
			flush();
			LOGGER.info("found {} response ok", count.get());
		}

		private void flush() {
			LOGGER.info("start flush");
			try {
				List<URI> requests = queue.stream().map(collaborator -> {
					try {
						String url = spUrl + "/_api/sp.userprofiles.peoplemanager/getpropertiesfor(@v)?@v='" + URLEncoder.encode(collaborator.getExternalId(), "UTF-8") + "'";
						return new URI(url);
					} catch (UnsupportedEncodingException | URISyntaxException e) {
						LOGGER.error("", e);
						return null;
					}
				}).collect(Collectors.toList());
				String token = appTokenManager.geAppTokenGenerator(spUrl, tenantId).getToken();
				batchAPI.processBatchObject(spUrl, token, requests, t -> t, new ICallBack<SPODataBatchResponse<String>>() {

					@Override
					public void processObject(SPODataBatchResponse<String> response) {
						if (!response.responseInError()) {
							count.incrementAndGet();
						} else {
							countError.incrementAndGet();
						}
					}
				});
			} catch (O365UserAuthenticationException e) {
				LOGGER.error("", e);
			}
		}
	}

	class CollaboratorCallback implements ICallBack<Collaborator> {

		private String			spUrl;

		private String			tenantId;

		private AtomicLong		count;

		private ExecutorService	threadPool;

		private final Integer	NB_THREAD	= 10;

		public CollaboratorCallback(String spUrl, String tenantId) {
			this.spUrl = spUrl;
			this.tenantId = tenantId;
			count = new AtomicLong(0);
			threadPool = Executors.newFixedThreadPool(NB_THREAD);
		}

		@Override
		public void processObject(Collaborator collaborator) {
			CollaboratorWorker worker = new CollaboratorWorker(tenantId, spUrl, count, collaborator);
			threadPool.execute(worker);
		}

		public void finish() {
			threadPool.shutdown();
			while (!threadPool.isTerminated()) {
			}
		}
	}

	class CollaboratorWorker implements Runnable {

		private String			spUrl;

		private String			tenantId;

		private AtomicLong		count;

		private Collaborator	collaborator;

		public CollaboratorWorker(String tenantId, String spUrl, AtomicLong count, Collaborator collaborator) {
			this.tenantId = tenantId;
			this.spUrl = spUrl;
			this.count = count;
			this.collaborator = collaborator;
		}

		@Override
		public void run() {
			try {
				SPUserProfile profile = userProfileAPI.getUserProfileProperties(spUrl, appTokenManager.geAppTokenGenerator(spUrl, tenantId).getToken(), collaborator.getExternalId());
				count.incrementAndGet();
			} catch (O365ConnectionException | O365HttpErrorException | O365UserAuthenticationException e) {
				e.printStackTrace();
			}
		}
	}

}
