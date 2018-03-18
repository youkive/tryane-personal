package com.tryane.saas.personal.extension.shrarepoint.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.connector.sharepoint.utils.api.SharepointAPI;
import com.tryane.saas.connector.sharepoint.utils.api.client.ISharepointAPIClient;
import com.tryane.saas.connector.sharepoint.utils.model.SPSiteUserCustomAction;

public class SPWebApiExtension implements ISPWebApiExtension {

	@Autowired
	protected ISharepointAPIClient spAPIClient;

	@Override
	public List<SPSiteUserCustomAction> getAllUserCustomActions(String webSiteUrl, String token) throws O365ConnectionException, O365HttpErrorException {
		JsonNode results = spAPIClient.call(webSiteUrl, token, SharepointAPI.WEB, "usercustomactions", JsonNode.class);
		return Lists.newArrayList(spAPIClient.getValue(getResultsNode(results), SPSiteUserCustomAction[].class));
	}

	protected JsonNode getResultsNode(JsonNode results) {
		return results.get("value");
	}

}
