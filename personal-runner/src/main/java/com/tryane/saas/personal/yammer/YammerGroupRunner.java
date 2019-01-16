package com.tryane.saas.personal.yammer;

import java.util.Set;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Joiner;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.esn.group.Group;
import com.tryane.saas.core.esn.group.Group.GroupPrivacy;
import com.tryane.saas.core.esn.group.IGroupManager;
import com.tryane.saas.core.esn.group.profile.GroupProperties;
import com.tryane.saas.core.esn.group.profile.GroupPropertyNames;
import com.tryane.saas.core.esn.group.profile.IGroupPropertiesManager;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.db.updater.config.DatabaseConfig;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.utils.joda.JodaUtils;

public class YammerGroupRunner {

	private static final Logger		LOGGER		= LoggerFactory.getLogger(YammerGroupRunner.class);

	private final String			NETWORK_ID	= "493683";

	private final String			GROUP_ID	= "14525433";

	@Autowired
	private INetworkManager			networkManager;

	@Autowired
	private IGroupManager			groupManager;

	@Autowired
	private IGroupPropertiesManager	groupPropertiesManager;

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, DatabaseConfig.class);
			YammerGroupRunner runner = new YammerGroupRunner();
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);
			runner.execute();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	public void execute() {
		Network currentNetwork = networkManager.getNetworkById(NETWORK_ID);
		ClientContextHolder.setNetwork(currentNetwork);
		//setGroupPrivate(GROUP_ID);
		displayInfo(GROUP_ID);
	}

	public void setGroupPrivate(String groupId) {
		Group group = groupManager.getGroup(GROUP_ID);
		if (group == null) {
			throw new RuntimeException("undefined group");
		}
		group.setPrivacy(GroupPrivacy.PRIVATE.name());
		groupManager.createOrUpdateGroup(group);
	}

	private void displayInfo(String groupId) {
		Group groupRequested = groupManager.getGroup(groupId);
		GroupProperties groupProperties = groupPropertiesManager.getGroupProperties(groupRequested, JodaUtils.getWeekIdentifier(LocalDate.now()));
		Set<String> membersIds = groupProperties.getPropertyValueAsSet(GroupPropertyNames.MEMBERS, new TypeReference<Set<String>>() {
		});
		LOGGER.info(Joiner.on(";").join(membersIds).toString());
	}
}
