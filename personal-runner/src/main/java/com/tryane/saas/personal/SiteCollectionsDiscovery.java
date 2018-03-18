package com.tryane.saas.personal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.connector.sharepoint.sitecollections.ISPAllSiteCollectionDiscoveryProcess;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class SiteCollectionsDiscovery {

	private static final String						NETWORK_ID	= "s1452";

	@Autowired
	private ISPAllSiteCollectionDiscoveryProcess	siteCollectionDiscoveryProcess;

	@Autowired
	private INetworkManager							networkManager;

	public static void main(String[] args) throws InterruptedException {
		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, PersonalDatabaseConfig.class);
			SiteCollectionsDiscovery runner = new SiteCollectionsDiscovery();
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);

			Network currentNetwork = runner.networkManager.getNetworkById(NETWORK_ID);
			ClientContextHolder.setNetwork(currentNetwork);

			runner.siteCollectionDiscoveryProcess.updateSiteCollectionListProcess();
		} catch (O365ConnectionException | O365HttpErrorException e) {
			e.printStackTrace();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}
}
