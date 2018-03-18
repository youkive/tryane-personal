package com.tryane.saas.personal.jira.saas2625;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.tryane.saas.connector.o365.utils.authentication.IO365Authenticator;
import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.connector.o365.utils.exception.O365UserAuthenticationException;
import com.tryane.saas.connector.sharepoint.utils.api.ISPWebAPI;
import com.tryane.saas.connector.sharepoint.utils.model.SharepointSPWeb;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.core.sp.site.ISPSiteManager;
import com.tryane.saas.core.sp.site.SPSite;
import com.tryane.saas.core.sp.site.SPSitePropertyNames;
import com.tryane.saas.core.sp.sitecol.ISPSiteCollectionManager;
import com.tryane.saas.core.sp.sitecol.SPSiteCollection;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class SAAS2625runner {

	private final String				NETWORK_ID			= "s443571";

	private final String				WEB_SITE_URL		= "https://biocodex1.sharepoint.com/sites/Communities/IT-France";

	private final String				SITE_COLLECTION_ID	= "ff5deeaa-cfd5-473e-9e83-8b43b8c6e95f";

	@Autowired
	private INetworkManager				networkManager;

	@Autowired
	private INetworkPropertyManager		networkPropertyManager;

	@Autowired
	private ISPSiteManager				webSiteManager;

	@Autowired
	private ISPSiteCollectionManager	siteCollectionManager;

	@Autowired
	private ISPWebAPI					spWebApi;

	@Autowired
	private IO365Authenticator			authenticator;

	public static void main(String[] args) throws O365ConnectionException, O365HttpErrorException, O365UserAuthenticationException {
		System.setProperty("spring.profiles.active", "dev");
		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, PersonalDatabaseConfig.class);

			SAAS2625runner runner = new SAAS2625runner();
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);

			runner.execute();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	public void execute() throws O365ConnectionException, O365HttpErrorException, O365UserAuthenticationException {
		Network network = networkManager.getNetworkById(NETWORK_ID);
		ClientContextHolder.setNetwork(network);

		String tenant = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_TENANT);
		String mainCollectionUrl = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_MAINCOLLECTION_URL);

		SPSiteCollection siteCollection = siteCollectionManager.getSPSiteCollectionById(SITE_COLLECTION_ID);

		AuthenticationResult authent = authenticator.getAppAccessToken(mainCollectionUrl, tenant);
		SharepointSPWeb webSiteApiObject = spWebApi.getWebObject(WEB_SITE_URL, authent.getAccessToken());

		mergeSPSiteWith(webSiteApiObject, siteCollection);
	}

	public SPSite mergeSPSiteWith(SharepointSPWeb webSite, SPSiteCollection spSiteCollection) {
		SPSite site = new SPSite();
		site.setId(webSite.getId());
		site.setCollectionId(spSiteCollection.getId());
		site.setUrl(webSite.getUrl());
		site.setName(webSite.getTitle());
		site.setCreationDate(LocalDate.parse(webSite.getCreatedAt(), ISODateTimeFormat.dateTimeParser()));
		site.setIsSupervised(false);

		ObjectNode properties = site.getProps();
		properties.put(SPSitePropertyNames.WEB_TEMPLATE, webSite.getWebTemplate());
		if (StringUtils.isNotBlank(webSite.getPowelltemplate())) {
			properties.put(SPSitePropertyNames.POWELL_TEMPLATE, webSite.getPowelltemplate());
		}
		return webSiteManager.createOrUpdate(site);
	}
}
