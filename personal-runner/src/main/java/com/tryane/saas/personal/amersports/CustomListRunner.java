package com.tryane.saas.personal.amersports;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.google.common.base.Joiner;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.core.sp.site.ISPSiteManager;
import com.tryane.saas.core.sp.site.SPSitePropertyNames;
import com.tryane.saas.core.sp.site.props.ISPSitePropertiesManager;
import com.tryane.saas.core.sp.site.props.SPSiteProperties;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class CustomListRunner {

	private static final Logger			LOGGER		= LoggerFactory.getLogger(CustomListRunner.class);

	private static final String			NETWORK_ID	= "s1";

	private static final LocalDate		WEEK_DATE	= LocalDate.parse("2017-01-29");

	@Autowired
	private INetworkManager				networkManager;

	@Autowired
	private ISPSitePropertiesManager	sitePropertiesManager;

	@Autowired
	private ISPSiteManager				spSiteManager;

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = null;
		CustomListRunner runner = new CustomListRunner();
		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, PersonalDatabaseConfig.class);
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);

			Network currentNetwork = runner.networkManager.getNetworkById(NETWORK_ID);
			ClientContextHolder.setNetwork(currentNetwork);

			Set<Long> customTypesFound = new HashSet<>();

			runner.spSiteManager.getAllValidSiteIds(WEEK_DATE).forEach(siteId -> {
				SPSiteProperties currentProperties = runner.sitePropertiesManager.getOrCreatePropertiesForWeek(siteId, WEEK_DATE);
				List<String> libTypes = currentProperties.getPropertyValues(SPSitePropertyNames.LIST_AND_LIB_TYPES, "#@#");

				for (String libType : libTypes) {
					if (Long.valueOf(libType) >= 10000) {
						LOGGER.info("{}: {}", siteId, libType);
						customTypesFound.add(Long.valueOf(libType));
					}
				}
			});

			LOGGER.info(Joiner.on(" , ").join(customTypesFound).toString());

		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

}
