package com.tryane.saas.personal.sharepoint.usercustomaction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.csvreader.CsvWriter;
import com.google.common.collect.Lists;
import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.connector.o365.utils.exception.O365UserAuthenticationException;
import com.tryane.saas.connector.o365.utils.token.IAppTokenManager;
import com.tryane.saas.connector.sharepoint.sitecollections.usercutomactions.registration.IUserCustomActionRegistrationManager;
import com.tryane.saas.connector.sharepoint.sitecollections.usercutomactions.remove.ISPJSInjectionManagerUtils;
import com.tryane.saas.connector.sharepoint.utils.api.ISPSiteAPI;
import com.tryane.saas.connector.sharepoint.utils.context.SPSiteCollectionContext;
import com.tryane.saas.connector.sharepoint.utils.model.SPSiteUserCustomAction;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.core.sp.sitecol.ISPSiteCollectionManager;
import com.tryane.saas.core.sp.sitecol.SPSiteCollection;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class SummaryUserCustomActionStateRunner extends AbstractSpringRunner {

    private static final Logger                  LOGGER           = LoggerFactory.getLogger(SummaryUserCustomActionStateRunner.class);

    private static final String                  NETWORK_ID       = "s140";

    private static final String                  RESSOURCE_FOLDER = "src/main/resources/com/tryane/saas/personal/sharepoint/usercustomactions";

    @Autowired
    private ISPSiteCollectionManager             siteCollectionManager;

    @Autowired
    private INetworkManager                      networkManager;

    @Autowired
    private INetworkPropertyManager              networkPropertyManager;

    @Autowired
    @Qualifier("jsUcaRegistrationManager")
    private IUserCustomActionRegistrationManager jsInjectionManger;

    @Autowired
    @Qualifier("spfxUcaRegistrationManager")
    private IUserCustomActionRegistrationManager spfxInjectionManager;

    @Autowired
    private IAppTokenManager                     appTokenManager;

    @Autowired
    private ISPSiteAPI                           siteApi;

    public static void main(String[] args) {
        new SummaryUserCustomActionStateRunner().runTest("", PersonalAppConfig.class, PersonalDatabaseConfig.class);
    }

    @Override
    protected void testImplementation() {
        ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));
        List<SiteCollectionState> states = Lists.newArrayList();

        String tenantId = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_TENANT);
        String spUrl = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_MAINCOLLECTION_URL);
        appTokenManager.initForTenant(tenantId);

        CsvWriter csvWriter = null;
        try (FileOutputStream outputStream = new FileOutputStream(getCsvFile())) {
            csvWriter = new CsvWriter(outputStream, ';', StandardCharsets.UTF_8);
            csvWriter.writeRecord(new String[] { "siteCollectionId", "siteCollectionUrl", "isMonitored", "nbJsUCA", "nbSPFXUca", "nbWSPUca" });
            for (SPSiteCollection siteCollection : siteCollectionManager.getAllSiteCollectionsNotDeleted()) {
                LOGGER.info("analyse {}", siteCollection.getUrl());
                try {
                    SiteCollectionState state = new SiteCollectionState(siteCollection);
                    SPSiteCollectionContext scContext = new SPSiteCollectionContext(siteCollection, appTokenManager.geAppTokenGenerator(spUrl, tenantId));
                    List<SPSiteUserCustomAction> userCustomActionRegistered = siteApi.getAllUserCustomActions(scContext.getCollectionUrl(), scContext.getToken());
                    for (SPSiteUserCustomAction uca : userCustomActionRegistered) {
                        state.isMonitoredByJs = jsInjectionManger.isUserCustomActionRegisteredByTryane(uca);
                        if (jsInjectionManger.isUserCustomActionRegisteredByTryane(uca)) {
                            state.nbMonitoredByJs++;
                        }
                        state.isMonitoredBySpfxAgent = spfxInjectionManager.isUserCustomActionRegisteredByTryane(uca);
                        if (spfxInjectionManager.isUserCustomActionRegisteredByTryane(uca)) {
                            state.nbMonitoredBySpfxAgent++;
                        }
                        state.isMonitoredByWSP = isWSPUca(uca);
                        if (isWSPUca(uca)) {
                            state.nbMonitoredByWSP++;
                        }
                    }
                    states.add(state);
                    csvWriter.writeRecord(new String[] { siteCollection.getId(), siteCollection.getUrl(), siteCollection.getIsSupervised().toString(), state.nbMonitoredByJs.toString(),
                            state.nbMonitoredBySpfxAgent.toString(), state.nbMonitoredByWSP.toString() });
                } catch (O365UserAuthenticationException e) {
                    LOGGER.error("", e);
                } catch (O365ConnectionException e) {
                    LOGGER.error("", e);
                } catch (O365HttpErrorException e) {
                    LOGGER.error("", e);
                } catch (IOException e) {
                    LOGGER.error("", e);
                }

            }
            if (csvWriter != null) {
                csvWriter.flush();
                csvWriter.close();
            }
        } catch (FileNotFoundException e1) {
            LOGGER.error("", e1);
        } catch (IOException e1) {
            LOGGER.error("", e1);
        }

        displayResult(states);
    }

    private void displayResult(List<SiteCollectionState> states) {
        states.forEach(state -> {
            LOGGER.info(state.siteCollection.getUrl());
            LOGGER.info("isMonitoredByJs : {}", state.isMonitoredByJs);
            LOGGER.info("isMonitoredByAddonAgent : {}", state.isMonitoredBySpfxAgent);
        });
    }

    class SiteCollectionState {
        SPSiteCollection siteCollection         = null;

        Boolean          isMonitoredByJs        = false;

        Long             nbMonitoredByJs        = 0L;

        Boolean          isMonitoredBySpfxAgent = false;

        Long             nbMonitoredBySpfxAgent = 0L;

        Boolean          isMonitoredByWSP       = false;

        Long             nbMonitoredByWSP       = 0L;

        public SiteCollectionState(SPSiteCollection siteCollection) {
            this.siteCollection = siteCollection;
        }
    }

    private Boolean isWSPUca(SPSiteUserCustomAction uca) {
        String tryaneServerUrl = "https://analytics.tryane.com/ta4sp";
        return org.apache.commons.lang3.StringUtils.isNotBlank(uca.getScriptBlock())
                && uca.getScriptBlock().contains(tryaneServerUrl)
                && (org.apache.commons.lang3.StringUtils.isBlank(uca.getTitle()) || !uca.getTitle().equals(ISPJSInjectionManagerUtils.buildTitleOfJsUserCustomAction(tryaneServerUrl)));
    }

    public File getCsvFile() throws IOException {
        String resourceURI = RESSOURCE_FOLDER + "/" + NETWORK_ID + "_" + LocalDate.now() + ".csv";
        Path filePath = Files.createFile(Paths.get(resourceURI));
        return filePath.toFile();
    }

}
