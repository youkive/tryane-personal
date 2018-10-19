package com.tryane.saas.personal.testapiretry;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.tryane.saas.connector.sharepoint.utils.api.client.SharepointOnlineAPIClient;

@Component("SharepointOnlineAPIClient")
@Primary
@Profile("mock-api")
public class SharePointOnlineMockApiClient extends SharepointOnlineAPIClient {

}
