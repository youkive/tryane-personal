package com.tryane.saas.personal.yammer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.tryane.saas.connector.yammer.api.exception.YammerConnectionException;
import com.tryane.saas.connector.yammer.api.exception.YammerHttpErrorException;
import com.tryane.saas.connector.yammer.common.model.user.YammerUser;
import com.tryane.saas.connector.yammer.newapi.IYammerUserAPI;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;
import com.tryane.saas.utils.hibernate.ICallBack;

public class ProcessAllYammerUserRunner {

	private static final Logger	LOGGER	= LoggerFactory.getLogger(ProcessAllYammerUserRunner.class);

	private static final String	TOKEN	= "297105-qqfryXsbhNGjenxgrJEg";

	@Autowired
	private IYammerUserAPI		userApi;

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = null;

		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, PersonalDatabaseConfig.class);
			ProcessAllYammerUserRunner runner = new ProcessAllYammerUserRunner();
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);

			try {
				runner.userApi.processAllUsers(TOKEN, new ICallBack<YammerUser>() {

					@Override
					public void processObject(YammerUser user) {
						if (user.isAdmin() && user.isVerifiedAdmin()) {
							LOGGER.info("{}", user.getEmail());
						}
					}
				});
			} catch (YammerConnectionException | YammerHttpErrorException e) {
				LOGGER.error("", e);
			}

		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}
}
