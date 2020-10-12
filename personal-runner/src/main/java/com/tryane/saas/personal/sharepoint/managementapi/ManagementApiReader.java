package com.tryane.saas.personal.sharepoint.managementapi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.tryane.saas.connector.managmentapi.model.IO365ManagementOperationLabel;
import com.tryane.saas.connector.managmentapi.model.O365ManagmentContent;
import com.tryane.saas.connector.o365.utils.token.IAppTokenManager;
import com.tryane.saas.connector.sharepoint.utils.api.ISPSearchAPI;
import com.tryane.saas.core.ClientContextHolder;
import com.tryane.saas.core.network.INetworkManager;
import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkPropertyNames;
import com.tryane.saas.core.sp.item.ISPItemManager;
import com.tryane.saas.core.sp.item.SPItem;
import com.tryane.saas.core.sp.list.ISPListManager;
import com.tryane.saas.core.sp.list.SPList;
import com.tryane.saas.core.sp.site.SPSitePK;
import com.tryane.saas.core.sp.sitecol.ISPSiteCollectionManager;
import com.tryane.saas.core.sp.sitecol.SPSiteCollection;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

import nl.basjes.shaded.com.google.common.collect.Lists;

public class ManagementApiReader extends AbstractSpringRunner {

    private static final Logger      LOGGER           = LoggerFactory.getLogger(ManagementApiReader.class);

    private static final String      NETWORK_ID       = "s443696";

    private static final LocalDate   START_DATE       = LocalDate.parse("2018-09-14");

    private static final String      RESSOURCE_FOLDER = "src/main/resources/com/tryane/saas/personal/sharepoint/managmentapi";

    private static final String      FILE_NAME_MAA    = "s443696_2018-09-24.csv";

    @Autowired
    private ISPSearchAPI             searchApi;

    @Autowired
    private INetworkManager          networkManager;

    @Autowired
    private INetworkPropertyManager  networkPropertyManager;

    @Autowired
    private ISPItemManager           itemManager;

    @Autowired
    private ISPSiteCollectionManager siteCollectionManager;

    @Autowired
    private IAppTokenManager         appTokenManager;

    @Autowired
    private ISPListManager           listManager;

    private CsvWriter                csvWriter        = null;

    public static void main(String[] args) {
        new ManagementApiReader().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
    }

    @Override
    protected void testImplementation() {
        ClientContextHolder.setNetwork(networkManager.getNetworkById(NETWORK_ID));
        String tenantId = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_TENANT);
        String sharepointUrl = networkPropertyManager.getNetworkPropertyValue(NETWORK_ID, NetworkPropertyNames.SHAREPOINT_MAINCOLLECTION_URL);
        appTokenManager.initForTenant(tenantId);

        List<SPItem> itemsToAnalyse = getItemstoAnalysed();
        LOGGER.info("{} items to analysed", itemsToAnalyse.size());
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(getCsvFile());
            csvWriter = new CsvWriter(outputStream, ';', StandardCharsets.UTF_8);
            csvWriter.writeRecord(Lists.newArrayList("fileName", "fileUniqId", "viewsLifeTime", "totalCountMAA", "fileAccessedCount", "filePreviewedCount", "filePageViewed", "externalUserAction")
                    .toArray(new String[0]));

            for (SPItem item : itemsToAnalyse) {
                try {
                    SPSitePK sitePk = new SPSitePK(item.getSiteId());
                    SPSiteCollection siteCollection = siteCollectionManager.getSPSiteCollectionById(sitePk.getCollectionId());
                    String token = appTokenManager.geAppTokenGenerator(sharepointUrl, tenantId).getToken();
                    String listId = item.getId().split("/")[0];
                    SPList list = listManager.getList(sitePk, listId);
                    searchApi.processAllDocsInList(getServerUrl(siteCollection), getRootUrlOfSiteCollection(siteCollection), token, list, document -> {
                        String uniqFileIdSearch = document.get("UniqueId");
                        String uniqFileId = uniqFileIdSearch.substring(1, uniqFileIdSearch.length() - 1).toLowerCase();
                        if (item.getFileUniqId().equals(uniqFileId)) {
                            String viewsLifeTimeString = document.get("ViewsLifeTime");
                            if (org.apache.commons.lang3.StringUtils.isBlank(viewsLifeTimeString)) {
                                return;
                            }
                            Long viewsLifeTime = viewsLifeTimeString.equals("Null") ? 0L : Long.parseLong(viewsLifeTimeString);
                            MaaRecordCounter counter = nbRecordAssociatedInMAA(item);
                            try {
                                csvWriter.writeRecord(Lists
                                        .newArrayList(item.getName(), item.getFileUniqId(), viewsLifeTime.toString(), counter.getTotalMaaCount().toString(), counter.fileAccessedCount
                                                .toString(), counter.filePreviewedCount.toString(), counter.pageViewedCount.toString(), counter.totalRecordForExternal.toString())
                                        .toArray(new String[0]));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    outputStream.flush();
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
            }

            //printOperationsAvailableInMaa();
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            try {
                if (csvWriter != null) {
                    csvWriter.flush();
                    csvWriter.close();
                }
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (Exception e) {
                LOGGER.info("", e);
            }
        }

    }

    private MaaRecordCounter nbRecordAssociatedInMAA(SPItem item) {
        CsvReader reader = null;
        try {
            reader = new CsvReader(RESSOURCE_FOLDER + "/" + FILE_NAME_MAA, ';');
            reader.readHeaders();
            MaaRecordCounter counter = new MaaRecordCounter();
            while (reader.readRecord()) {
                O365ManagmentContent content = mapToContent(reader);
                Boolean isAssociated = item.getFileUniqId().toLowerCase().equals(content.getListItemUniqId());

                if (isAssociated) {
                    counter.totalRecordCount++;
                    switch (content.getOperation()) {
                    case IO365ManagementOperationLabel.FILE_ACCESSED:
                        counter.fileAccessedCount++;
                        break;
                    case IO365ManagementOperationLabel.FILE_ACCESSED_EXTENDED:
                        counter.fileAccessedExtendedCount++;
                        break;
                    case IO365ManagementOperationLabel.FILE_CHECKOUT:
                        counter.fileCheckoutCount++;
                        break;
                    case IO365ManagementOperationLabel.FILE_DOWNLOADED:
                        counter.fileDownloadedCount++;
                        break;
                    case IO365ManagementOperationLabel.FILE_PREVIEWED:
                        counter.filePreviewedCount++;
                        break;
                    case IO365ManagementOperationLabel.FILE_SYNC_DOWNLOADED_FULL:
                        counter.fileSyncDownloadedFullCount++;
                        break;
                    case IO365ManagementOperationLabel.PAGE_VIEWED:
                        counter.pageViewedCount++;
                        break;
                    default:
                        break;
                    }

                    if (content.getUserId().toLowerCase().contains("#ext#")) {
                        counter.totalRecordForExternal++;
                    }
                }
            }
            return counter;
        } catch (IOException e) {
            LOGGER.error("", e);
            return null;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    class MaaRecordCounter {
        public Long totalRecordCount            = 0L;

        public Long totalRecordForExternal      = 0L;

        public Long fileAccessedCount           = 0L;

        public Long fileAccessedExtendedCount   = 0L;

        public Long filePreviewedCount          = 0L;

        public Long fileCheckoutCount           = 0L;

        public Long fileDownloadedCount         = 0L;

        public Long fileSyncDownloadedFullCount = 0L;

        public Long pageViewedCount             = 0L;

        public Long getTotalMaaCount() {
            if (pageViewedCount > 0) {
                return pageViewedCount;
            }
            return fileAccessedCount + filePreviewedCount + fileAccessedExtendedCount;
        }

    }

    public File getCsvFile() throws IOException {
        String resourceURI = RESSOURCE_FOLDER + "/" + NETWORK_ID + "_results_" + LocalDate.now() + ".csv";
        Path filePath = Files.createFile(Paths.get(resourceURI));
        return filePath.toFile();
    }

    public void printOperationsAvailableInMaa() {
        CsvReader reader = null;
        Set<String> operations = new HashSet<>();

        try {
            reader = new CsvReader(RESSOURCE_FOLDER + "/" + FILE_NAME_MAA, ';');
            reader.readHeaders();
            while (reader.readRecord()) {
                O365ManagmentContent content = mapToContent(reader);
                operations.add(content.getOperation());
            }
        } catch (IOException e) {
            LOGGER.error("", e);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        LOGGER.info("operation found");
        operations.forEach(operation -> LOGGER.info("{}", operation));
    }

    private List<SPItem> getItemstoAnalysed() {
        List<SPItem> itemsToAnalyse = Lists.newArrayList();
        itemManager.processAllItems(item -> {
            if (org.apache.commons.lang3.StringUtils.isBlank(item.getDataValue("creationdate"))) {
                return;
            }
            LocalDate creationDateItem = LocalDate.parse(item.getDataValue("creationdate"));
            if (!creationDateItem.isBefore(START_DATE)) {
                itemsToAnalyse.add(item);
            }
        });
        return itemsToAnalyse;
    }

    private O365ManagmentContent mapToContent(CsvReader csvReader) throws IOException {
        O365ManagmentContent content = new O365ManagmentContent();
        content.setListItemUniqId(csvReader.get("ListItemUniqId"));
        content.setOperation(csvReader.get("Operation"));
        content.setCreationTime(csvReader.get("CreationTime"));
        content.setUserId(csvReader.get("UserId"));
        return content;
    }

    protected String getRootUrlOfSiteCollection(SPSiteCollection siteCollection) {
        return siteCollection.getRootUrl();
    }

    /**
     * Obtenir l'url du serveur => premier segment de la collection de sites pour le online
     */
    protected String getServerUrl(SPSiteCollection siteCollection) {
        return siteCollection.getRootUrl();
    }
}
