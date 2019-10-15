package com.tryane.saas.personal.sharepoint.managementapi;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.csvreader.CsvWriter;
import com.google.common.collect.Lists;
import com.tryane.saas.connector.managmentapi.api.IO365ManagmentActivityApi;
import com.tryane.saas.connector.managmentapi.model.O365ManagmentContent;
import com.tryane.saas.connector.managmentapi.model.O365ManagmentContentBlob;
import com.tryane.saas.connector.o365.utils.IO365Resources;
import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.connector.o365.utils.exception.O365UserAuthenticationException;
import com.tryane.saas.connector.o365.utils.token.AppTokenManager;
import com.tryane.saas.connector.sharepoint.process.o365managmentapi.callback.O365ManagmentApiBlobMultiThreadCallback;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.utils.hibernate.ICallBack;
import com.tryane.saas.utils.multithreading.TryaneThreadFactoryBuilder;

public class ManagmentApiMultiThreadCallback implements ICallBack<O365ManagmentContentBlob> {
	private static final Logger			LOGGER		= LoggerFactory.getLogger(O365ManagmentApiBlobMultiThreadCallback.class);

	@Autowired
	private IO365ManagmentActivityApi	o365ManagementActivityApi;

	@Autowired
	private INetworkPropertyManager		networkPropertyManager;

	@Autowired
	private AppTokenManager				appTokenManager;

	private ExecutorService				threadPool	= Executors.newFixedThreadPool(10);

	private String						tenantId;

	private CsvWriter					csvWriter;

	public ManagmentApiMultiThreadCallback(CsvWriter csvWriter) {
		this.csvWriter = csvWriter;
		try {
			this.csvWriter.writeRecord(new String[] { "Id", "Operation", "SiteId", "WebId", "ListId", "ObjectId", "ListItemUniqId", "UserId", "CreationTime" });
		} catch (IOException e) {
			LOGGER.error("", e);
		}
	}

	@PostConstruct
	public void init() throws O365UserAuthenticationException {
		threadPool = Executors.newFixedThreadPool(10, new TryaneThreadFactoryBuilder("sp-o365-geteventcontent-" + ClientContextHolder.getNetworkId()).build());
		this.tenantId = networkPropertyManager.getNetworkPropertyValue(ClientContextHolder.getNetworkId(), NetworkPropertyNames.SHAREPOINT_TENANT);
		appTokenManager.initForTenant(tenantId);
	}

	public void finish() {
		if (threadPool != null) {
			threadPool.shutdown();
			try {
				threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			} catch (Exception e) {
				LOGGER.error("", e);
			}
		}
	}

	@Override
	public void processObject(O365ManagmentContentBlob objectToProcess) {
		try {
			ThreadRunnable thread = new ThreadRunnable(ClientContextHolder.getNetwork(), objectToProcess, appTokenManager.geAppTokenGenerator(IO365Resources.MANAGMENT_API_RESOURCE, this.tenantId)
					.getToken());
			threadPool.execute(thread);
		} catch (O365UserAuthenticationException e) {
			LOGGER.error("", e);
		}
	}

	class ThreadRunnable implements Runnable {

		private O365ManagmentContentBlob	blob;

		private String						managementApiToken;

		private Network						network;

		public ThreadRunnable(Network network, O365ManagmentContentBlob blob, String managementApiToken) {
			this.blob = blob;
			this.managementApiToken = managementApiToken;
			this.network = network;
		}

		@Override
		public void run() {
			ClientContextHolder.setNetwork(network);
			try {
				List<O365ManagmentContent> contents = o365ManagementActivityApi.getContentsInBlob(managementApiToken, blob.getContentUri());
				contents.forEach(content -> {
					try {
						List<String> record = Lists.newArrayList(content.getId(), content.getOperation(), content.getSite(), content.getWebId(), content.getListId(), content.getObjectId(), content
								.getListItemUniqId(), content.getUserId(), content.getCreationTime());
						synchronized (this) {
							writeInCsv(record.toArray(new String[0]));
						}
					} catch (Exception e) {
						LOGGER.info("", e);
					}
				});
			} catch (O365ConnectionException | O365HttpErrorException e) {
				LOGGER.error("", e);
			} catch (Exception e) {
				LOGGER.debug("", e);
			}
		}
	}

	public synchronized void writeInCsv(String[] toWrite) throws IOException {
		csvWriter.writeRecord(toWrite);
	}
}
