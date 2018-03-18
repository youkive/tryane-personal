package com.tryane.saas.personal.yammer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.esn.group.Group;
import com.tryane.saas.core.esn.group.Group.GroupPrivacy;
import com.tryane.saas.core.esn.group.IGroupManager;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.db.updater.config.DatabaseConfig;
import com.tryane.saas.personal.config.PersonalAppConfig;

public class YammerGroupRunner {

	private final String	NETWORK_ID	= "1897800";

	private final String	GROUP_ID	= "5214665";

	@Autowired
	private INetworkManager	networkManager;

	@Autowired
	private IGroupManager	groupManager;

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
		setGroupPrivate(GROUP_ID);
	}

	public void setGroupPrivate(String groupId) {
		Group group = groupManager.getGroup(GROUP_ID);
		if (group == null) {
			throw new RuntimeException("undefined group");
		}
		group.setPrivacy(GroupPrivacy.PRIVATE.name());
		groupManager.createOrUpdateGroup(group);
	}
}
