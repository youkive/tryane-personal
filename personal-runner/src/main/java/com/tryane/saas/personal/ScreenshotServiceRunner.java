package com.tryane.saas.personal;

import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.core.user.IUserManager;
import com.tryane.saas.core.user.User;
import com.tryane.saas.core.user.token.IUserTokenManager;
import com.tryane.saas.core.user.token.UserToken;
import com.tryane.saas.core.user.token.UserToken.TokenSource;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;
import com.tryane.saas.utils.screenshot.IScreenshotService;

public class ScreenshotServiceRunner extends AbstractSpringRunner {

	@Autowired
	private IScreenshotService	screenshotService;

	@Autowired
	private IUserManager		userManager;

	@Autowired
	private IUserTokenManager	userTokenManager;

	@Override
	protected void testImplementation() {
		User user = userManager.getUserById(437L);
		UserToken token = userTokenManager.registerUserToken(user, TokenSource.EXPORT_IMPERSONATION);
		screenshotService.screenshotElement(token.getToken(), "/export/notification-email/500603", 720, ".has-dashboard");
	}

	public static void main(String[] args) {
		new ScreenshotServiceRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}
}
