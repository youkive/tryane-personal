package com.tryane.saas.personal.sharepoint.managementapi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.csvreader.CsvWriter;
import com.tryane.saas.connector.managmentapi.api.IO365ManagmentActivityApi;
import com.tryane.saas.connector.managmentapi.api.O365ManagmentContentType;
import com.tryane.saas.connector.managmentapi.api.O365ManagmentProcessContext;
import com.tryane.saas.connector.o365.utils.IO365Resources;
import com.tryane.saas.connector.o365.utils.authentication.IO365Authenticator;
import com.tryane.saas.connector.o365.utils.exception.O365UserAuthenticationException;
import com.tryane.saas.connector.o365.utils.token.IO365TokenSupplier;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class ManagementApiRunner extends AbstractSpringRunner {

	private static final Logger			LOGGER				= LoggerFactory.getLogger(ManagementApiRunner.class);

	private static final String			NETWORK_ID			= "s27824";

	private static final String			RESSOURCE_FOLDER	= "src/main/resources/com/tryane/saas/personal/sharepoint/managmentapi";

	@Autowired
	private ApplicationContext			applicationContext;

	@Autowired
	private INetworkManager				networkManager;

	@Autowired
	private INetworkPropertyManager		networkPropertyManager;

	@Autowired
	private IO365ManagmentActivityApi	managmentActivityApi;

	@Autowired
	private IO365Authenticator			authenticator;

	public static void main(String[] args) {
		new ManagementApiRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

	@Override
	protected void testImplementation() {
		FileOutputStream outputStream = null;
		CsvWriter csvWriter = null;
		try {
			ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));
			String tenantId = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_TENANT);
			String token = authenticator.getDelegateAuthenticator().getAppAccessToken(IO365Resources.MANAGMENT_API_RESOURCE, tenantId).getAccessToken();

			outputStream = new FileOutputStream(getCsvFile());
			csvWriter = new CsvWriter(outputStream, ';', StandardCharsets.UTF_8);

			O365ManagmentProcessContext context = new O365ManagmentProcessContext();
			context.setTenantId(tenantId);
			context.setStartDate(LocalDate.parse("2018-09-10"));
			context.setEndDate(LocalDate.now().plusDays(1));
			context.setManagementApiTokenSupplier(new IO365TokenSupplier() {

				@Override
				public String getToken() throws O365UserAuthenticationException {
					return token;
				}
			});

			ManagmentApiMultiThreadCallback callback = new ManagmentApiMultiThreadCallback(csvWriter);
			applicationContext.getAutowireCapableBeanFactory().autowireBean(callback);
			applicationContext.getAutowireCapableBeanFactory().initializeBean(callback, "callbackMultiThread");
			managmentActivityApi.processAllBlobsForSubscription(context, O365ManagmentContentType.SHAREPOINT, callback);
			callback.finish();
		} catch (Exception e) {
			LOGGER.error("", e);
		} finally {
			try {
				if (csvWriter != null) {
					csvWriter.flush();
					csvWriter.close();
				}
				if (outputStream != null) {
					outputStream.flush();
					outputStream.close();
				}
			} catch (Exception e) {
				LOGGER.error("", e);
			}
		}
	}

	public File getCsvFile() throws IOException {
		String resourceURI = RESSOURCE_FOLDER + "/" + NETWORK_ID + "_" + LocalDate.now() + ".csv";
		Path filePath = Files.createFile(Paths.get(resourceURI));
		return filePath.toFile();
	}
}
