package com.tryane.saas.personal.extension.shrarepoint.api;

import java.util.List;

import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.connector.sharepoint.utils.model.SPSiteUserCustomAction;

public interface ISPWebApiExtension {

	public List<SPSiteUserCustomAction> getAllUserCustomActions(String webSiteUrl, String token) throws O365ConnectionException, O365HttpErrorException;
}
