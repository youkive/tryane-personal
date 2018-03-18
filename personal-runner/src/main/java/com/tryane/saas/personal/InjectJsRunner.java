package com.tryane.saas.personal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.tryane.saas.connector.o365.utils.authentication.IO365Authenticator;
import com.tryane.saas.connector.sharepoint.utils.api.ISPSiteAPI;

public class InjectJsRunner {

	public String				tenant				= "";

	public String				siteCollectionUrl	= "";

	@Autowired
	private IO365Authenticator	o365Authenticator;

	@Autowired
	private ISPSiteAPI			siteAPI;

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext();

			InjectJsRunner runner = new InjectJsRunner();
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);

		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}
}
