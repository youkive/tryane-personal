package com.tryane.saas.personal.sharepoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.core.sp.item.ISPItemManager;
import com.tryane.saas.core.sp.item.SPItem;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

/**
 * Vérifier que pur tout item où l'on a un uniq id et qui n'est pas supprimé, nous avons bien un nombre
 * de vue trouvé par le search
 */
public class CheckNbViewsOnItemRunner {

	private static final Logger	LOGGER		= LoggerFactory.getLogger(CheckNbViewsOnItemRunner.class);

	private static final String	NETWORK_ID	= "s1";

	@Autowired
	private INetworkManager		networkManager;

	@Autowired
	private ISPItemManager		itemManager;

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, PersonalDatabaseConfig.class);
			CheckNbViewsOnItemRunner runner = new CheckNbViewsOnItemRunner();
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);

			Network currentNetwork = runner.networkManager.getNetworkById(NETWORK_ID);
			ClientContextHolder.setNetwork(currentNetwork);

			for (SPItem item : runner.itemManager.findAllItems()) {
				if (item.getFileUniqId() != null && !item.getFileUniqId().isEmpty()) {
					// Commenté car la branche aec le code a testé n'est pas mergée
					//					String nbViews = item.getDataValue(EsnItemProperties.SP_NB_VIEWS);
					//					if (nbViews == null || nbViews.isEmpty()) {
					//						String urlItem = item.getDataValue(EsnItemProperties.WEB_URL);
					//						LOGGER.error("Arffffff : {} || {}", urlItem, item.getFileUniqId());
					//					}
				}
			}
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}
}
