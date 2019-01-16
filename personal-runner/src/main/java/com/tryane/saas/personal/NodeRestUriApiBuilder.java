package com.tryane.saas.personal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeRestUriApiBuilder {

	private static final Logger	LOGGER							= LoggerFactory.getLogger(NodeRestUriApiBuilder.class);

	private static final String	NODE_REST_URL					= "http://localhost:3000";

	private static final String	ROUTE_PUPPETEER_EXPORT_IMAGE	= "/puppeteer/export-image";

	private static final String	TOKEN_USER						= "aadefezgzgrgrzehheh";

	private static final String	NETWORK_ID						= "941684";

	public static void main(String[] args) {
		String saasFullUrl = "https%3A%2F%2Fanalytics.tryane.com%3Ftoken%3D" + TOKEN_USER + "%23%2Fexport%2Fnotification-email%2F" + NETWORK_ID;

		StringBuilder nodeRestUriBuilder = new StringBuilder(NODE_REST_URL);
		nodeRestUriBuilder.append(ROUTE_PUPPETEER_EXPORT_IMAGE);
		nodeRestUriBuilder.append("?width=").append(720);
		nodeRestUriBuilder.append("&selector=").append(".has-dashboard");
		nodeRestUriBuilder.append("&fronturl=").append(saasFullUrl);

		LOGGER.info("{}", nodeRestUriBuilder.toString());
	}
}
