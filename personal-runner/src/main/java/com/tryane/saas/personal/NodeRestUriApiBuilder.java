package com.tryane.saas.personal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.UrlEscapers;

public class NodeRestUriApiBuilder {

	private static final Logger	LOGGER							= LoggerFactory.getLogger(NodeRestUriApiBuilder.class);

	private static final String	NODE_REST_URL					= "http://localhost:3000";

	private static final String	ROUTE_PUPPETEER_EXPORT_IMAGE	= "/puppeteer/export-image";

	public static void main(String[] args) {
		String saasFullUrl = "https://analytics.tryane.com/?token=1251a9c75ec5ea282ed33da4e20386ab030c894c#/export/notification-email/500603";

		StringBuilder nodeRestUriBuilder = new StringBuilder(NODE_REST_URL);
		nodeRestUriBuilder.append(ROUTE_PUPPETEER_EXPORT_IMAGE);
		nodeRestUriBuilder.append("?width=").append(720);
		nodeRestUriBuilder.append("&selector=").append(".has-dashboard");
		nodeRestUriBuilder.append("&fronturl=").append(UrlEscapers.urlFragmentEscaper().escape(saasFullUrl));

		LOGGER.info("{}", nodeRestUriBuilder.toString());
	}
}
