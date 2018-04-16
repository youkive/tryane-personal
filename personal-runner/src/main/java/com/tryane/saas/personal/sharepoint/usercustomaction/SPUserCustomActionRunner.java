package com.tryane.saas.personal.sharepoint.usercustomaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.tryane.saas.connector.o365.utils.authentication.IO365Authenticator;
import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.connector.o365.utils.exception.O365UserAuthenticationException;
import com.tryane.saas.connector.sharepoint.utils.api.ISPSiteAPI;
import com.tryane.saas.connector.sharepoint.utils.model.SPSiteUserCustomAction;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.Network;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.core.sp.sitecol.SPSiteCollection;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class SPUserCustomActionRunner {

	@Autowired
	private INetworkManager			networkManager;

	@Autowired
	private INetworkPropertyManager	networkPropertyManager;

	@Autowired
	private ISPSiteAPI				siteApi;

	@Autowired
	private IO365Authenticator		o365Authenticator;

	@Value("${ta4sp.webapp.dev}")
	private String					ta4spWebappUrl;

	private static final String		USERCUSTOMACTION_LOCATION	= "ScriptLink";

	private static final String		USERCUSTOMACTION_SEQUENCE	= "100";

	private static final String		NETWORK_ID					= "s1";

	private static final String		SITE_COLLECTION_URL			= "https://tryane211.sharepoint.com/sites/modernui6";

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "dev");

		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class, PersonalDatabaseConfig.class);
			SPUserCustomActionRunner runner = new SPUserCustomActionRunner();
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);

			runner.executeRegisterOnSiteCollection(SITE_COLLECTION_URL);
			//runner.deleteUserCustomActionOnSiteCollection(SITE_COLLECTION_URL, "5a56930a-dc49-41b3-9656-e9ee9598587e");
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	public void executeRegisterOnSiteCollection(String siteCollectionUrl) {
		Network network = networkManager.getNetworkById(NETWORK_ID);
		ClientContextHolder.setNetwork(network);

		String tenantId = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_TENANT);

		String token = null;
		try {
			SPSiteCollection siteCollection = new SPSiteCollection();
			siteCollection.setUrl(siteCollectionUrl);

			token = o365Authenticator.getDelegateAuthenticator().getAppAccessToken(siteCollection.getRootUrl(), tenantId).getAccessToken();
		} catch (O365UserAuthenticationException e1) {
			e1.printStackTrace();
		}

		SPSiteUserCustomAction userCustomAction = new SPSiteUserCustomAction();
		userCustomAction.setTitle("BastienTrial");
		userCustomAction.setName("BastienTrial");
		//userCustomAction.setScriptBlock(buildJsScript(ta4spWebappUrl));
		userCustomAction.setLocation("ClientSideExtension.ApplicationCustomizer");
		userCustomAction.setSequence(USERCUSTOMACTION_SEQUENCE);
		userCustomAction.setClientSideComponentId("a44a888a-8ba8-428d-a309-853b5a3aa521");
		try {
			siteApi.registerUserCustomAction(siteCollectionUrl, token, userCustomAction);
		} catch (O365ConnectionException | O365HttpErrorException e) {
			e.printStackTrace();
		}

	}

	public void deleteUserCustomActionOnSiteCollection(String siteCollectionUrl, String userCustomActionId) {
		Network network = networkManager.getNetworkById(NETWORK_ID);
		ClientContextHolder.setNetwork(network);

		String tenantId = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_TENANT);
		String ressourceSharepoint = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_MAINCOLLECTION_URL);

		String token = null;
		try {
			token = o365Authenticator.getDelegateAuthenticator().getAppAccessToken(ressourceSharepoint, tenantId).getAccessToken();
		} catch (O365UserAuthenticationException e1) {
			e1.printStackTrace();
		}

		try {
			siteApi.deleteUserCustomAction(siteCollectionUrl, token, userCustomActionId);
		} catch (O365ConnectionException | O365HttpErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected String buildJsScript(String ta4spWebappUrl) {
		String sharepointNetworkType = ClientContextHolder.getNetwork().getPropertyInPropsAsString(NetworkPropertyNames.O365_PRODUCT_SUB_TYPE);

		StringBuilder script = new StringBuilder("var ta4spToken='");
		script.append(ClientContextHolder.getNetworkId());
		script.append("';");
		script.append("var d = new Date();d.setMinutes(0);d.setSeconds(0);d.setMilliseconds(0);");
		//jquery
		script.append(buildJsScriptElement("/dist/jquery.js'", ta4spWebappUrl));
		//ta4sp
		script.append(buildJsScriptElement("/dist/" + sharepointNetworkType + "/ta4sp-agent.min.js?networkid=' + ta4spToken + '&ts=' + d.getTime()", ta4spWebappUrl));

		return script.toString();

	}

	protected String buildJsScriptElement(String jsFilePath, String ta4spWebappUrl) {
		StringBuilder element = new StringBuilder();
		element.append("var script = document.createElement('script');script.type = 'application/javascript';script.src = '");
		element.append(ta4spWebappUrl).append(jsFilePath);
		element.append(";document.getElementsByTagName('head')[0].appendChild(script);");
		return element.toString();
	}
}
