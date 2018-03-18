package com.tryane.saas.personal.jive;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.esn.group.Group;
import com.tryane.saas.core.esn.group.IGroupManager;
import com.tryane.saas.core.jive.place.props.JiveGroupPropertiesName;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class JivePlaceDecryptorRunner {

	public static final Logger	LOGGER		= LoggerFactory.getLogger(JivePlaceDecryptorRunner.class);

	public static final String	NETWORK_ID	= "j70459";

	@Autowired
	private INetworkManager		networkManager;

	@Autowired
	private IGroupManager		groupManager;

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, PersonalDatabaseConfig.class);
			JivePlaceDecryptorRunner runner = new JivePlaceDecryptorRunner();
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);

			runner.execute();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	public void execute() {
		ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));

		displayPlaceInfo("149449");
		displayPlaceInfo("149450");
	}

	public void displayPlaceInfo(String placeId) {
		Group group = groupManager.getGroup(placeId);
		String placeType = group.getPropertyValueAsString(JiveGroupPropertiesName.JIVE_PLACE_TYPE);

		Set<String> childPlacesIds = group.getDataPropSet(JiveGroupPropertiesName.JIVE_CHILDREN_PLACE_IDS, String.class);
		childPlacesIds = childPlacesIds == null ? Sets.newHashSet() : childPlacesIds;

		LOGGER.info("Group Id: {} | groupName : {} | placeType: {}", group.getGroupId(), group.getName(), placeType);
		LOGGER.info("Child Group Ids : {}", Joiner.on(",").join(childPlacesIds));
		LOGGER.info("");
	}
}
