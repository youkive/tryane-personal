package com.tryane.saas.personal.sharepoint.item;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;
import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.connector.o365.utils.exception.O365UserAuthenticationException;
import com.tryane.saas.connector.o365.utils.token.IAppTokenManager;
import com.tryane.saas.connector.sharepoint.utils.api.ISPWebListAPI;
import com.tryane.saas.connector.sharepoint.utils.model.SPItemFolder;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.core.sp.item.SPItem;
import com.tryane.saas.core.sp.site.ISPSiteManager;
import com.tryane.saas.core.sp.site.SPSite;
import com.tryane.saas.core.sp.site.SPSitePK;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;
import com.tryane.saas.personal.sharepoint.manager.IPersonalSPItemManager;
import com.tryane.saas.utils.hibernate.ICallBack;

@Component
public class SPItemProcessRunner extends AbstractSpringRunner {

	private static final Logger		LOGGER		= LoggerFactory.getLogger(SPItemProcessRunner.class);

	private final String			NETWORK_ID	= "s1";

	@Autowired
	private INetworkManager			networkManager;

	@Autowired
	private IPersonalSPItemManager	personalItemManager;

	@Autowired
	private ISPSiteManager			siteManager;

	@Autowired
	private ISPWebListAPI			spWebListAPI;

	@Autowired
	private IAppTokenManager		tokenManager;

	@Autowired
	private INetworkPropertyManager	networkPropertyManager;

	@Override
	protected void testImplementation() {
		Network network = networkManager.getNetworkById(NETWORK_ID);
		ClientContextHolder.setNetwork(network);
		processItems();
	}

	public static void main(String[] args) {
		new SPItemProcessRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

	public void processItems() {
		AtomicLong folderCount = new AtomicLong();
		AtomicLong unknownCount = new AtomicLong();
		String tenantId = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_TENANT);
		String sharePointResource = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_MAINCOLLECTION_URL);
		tokenManager.initForTenant(tenantId);

		Set<String> idtoAnalyse = new HashSet<String>();

		personalItemManager.processAllItems(new ICallBack<SPItem>() {

			@Override
			public void processObject(SPItem item) {
				if (item.getSize() != null) {
					return;
				}
				SPSite webSite = siteManager.getSPSiteById(new SPSitePK(item.getSiteId()));
				try {
					SPItemFolder folder = spWebListAPI
							.getItemFolderInList(webSite.getUrl(), tokenManager.geAppTokenGenerator(sharePointResource, tenantId).getToken(), item.getId().split("/")[0], item.getId().split("/")[1]);
					if (folder != null) {
						folderCount.getAndIncrement();
						LOGGER.info("FIND FOLDER");
					} else {
						unknownCount.getAndIncrement();
						idtoAnalyse.add(item.getSpItemPK().getSiteId() + "/" + item.getId());
					}
				} catch (O365ConnectionException | O365HttpErrorException | O365UserAuthenticationException e) {
					LOGGER.error("error to retrieve item", e);
				}
			}
		});

		LOGGER.info("found {} items with folders", folderCount.get());
		LOGGER.info("found {} items unknown", unknownCount.get());
		LOGGER.info(Joiner.on("#").join(idtoAnalyse));
	}

}
