package com.tryane.saas.personal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tryane.saas.core.user.User;

public class PasswordUserGenerator {

	private static final Logger	LOGGER				= LoggerFactory.getLogger(PasswordUserGenerator.class);

	private static final String	PASSWORD_TO_ENCODE	= "timpwd";

	public static void main(String[] args) {
		User user = new User();
		user.setPlainPassword(PASSWORD_TO_ENCODE);
		user.hashPassword();
		LOGGER.info(user.getPassword());
	}
}
