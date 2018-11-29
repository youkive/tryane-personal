package com.tryane.saas.personal;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.tryane.saas.core.client.IClientManager;
import com.tryane.saas.core.user.IUserManager;
import com.tryane.saas.core.user.User;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class SearchUserRunner {

	@Autowired
	private IUserManager		userManager;

	@Autowired
	private IClientManager		clientManager;

	private static final Logger	LOGGER	= LoggerFactory.getLogger(SearchUserRunner.class);

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = null;

		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, PersonalDatabaseConfig.class);

			SearchUserRunner runner = new SearchUserRunner();
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);

			runner.execute();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	public void execute() {
		//displayUserInfo(6852L);
		searchUserPartOfEmail("krishnashetty");
	}

	private void searchUserPartOfEmail(String textToFindInMail) {
		for (Long clientId : clientManager.getAllClientIds()) {
			List<User> users = userManager.getAllUsers(clientId);
			for (User user : users) {
				if (user.getMailAddress().contains(textToFindInMail)) {
					displayUserInfo(user.getId());
				}
			}
		}
	}

	private void displayUserInfo(Long userId) {
		User user = userManager.getUserById(userId);
		LOGGER.info("user Id : {}", user.getId());
		LOGGER.info("email : {}", user.getMailAddress());
		LOGGER.info("login ext : {}", user.getLogin());
		LOGGER.info("{}", user.getData().toString());
	}
}
