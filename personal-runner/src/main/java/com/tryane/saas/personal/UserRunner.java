package com.tryane.saas.personal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.tryane.saas.core.client.IClientManager;
import com.tryane.saas.core.user.IUserManager;
import com.tryane.saas.core.user.User;
import com.tryane.saas.core.user.UserPropertiesByNetwork;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class UserRunner {

	private static final Logger	LOGGER			= LoggerFactory.getLogger(UserRunner.class);

	private static final Long	USER_ID_BASTIEN	= 6L;

	@Autowired
	private IUserManager		userManager;

	@Autowired
	private IClientManager		clientManager;

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, PersonalDatabaseConfig.class);
			UserRunner runner = new UserRunner();
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);

			runner.execute();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	public void displayUser(User user) {
		LOGGER.info("user id : {}", user.getId());
		LOGGER.info("client id : {}", user.getId());
		LOGGER.info("nom : {}", user.getDisplayName());
		LOGGER.info("mail: {}", user.getMailAddress());
		LOGGER.info("props : {}", user.getData().toString());
	}

	public void execute() {
		//User user = userManager.getUserByEmail("bastien.vaneenaeme@tryane.com");
		User user = userManager.getUserById(USER_ID_BASTIEN);
		//displayUser(user);

		displayUsersOfClient(1L);
		//displayAllUsersWithEmail("usclaro");
	}

	public void setGroupAdminProperties() {
		User user = userManager.getUserById(437L);
		setPropertyForNetwork(user, "500603", UserPropertiesByNetwork.TOKEN, null);
		setPropertyForNetwork(user, "500603", UserPropertiesByNetwork.GROUP_ADMIN, null);
		userManager.updateUser(user);
	}

	private void displayUsersOfClient(Long clientId) {
		userManager.getAllUsers(clientId).forEach(user -> {
			displayUser(user);
		});
	}

	private void displayAllUsersWithEmail(String email) {
		clientManager.getAllClientIds().forEach(clientId -> {
			userManager.getAllUsers(clientId).stream().filter(user -> {
				return user.getMailAddress().contains(email);
			}).forEach(user -> {
				displayUser(user);
			});
		});
	}

	private void setPropertyForNetwork(User user, String networkId, UserPropertiesByNetwork networkPropertyName, Object value) {
		user.setNetworkDataProp(networkId, networkPropertyName, value);
	}
}
