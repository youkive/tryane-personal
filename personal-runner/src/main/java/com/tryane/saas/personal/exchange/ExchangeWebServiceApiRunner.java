package com.tryane.saas.personal.exchange;

import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.ws.Holder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.schemas.exchange.services._2006.messages.FolderInfoResponseMessageType;
import com.microsoft.schemas.exchange.services._2006.messages.GetFolderResponseType;
import com.microsoft.schemas.exchange.services._2006.messages.GetFolderType;
import com.microsoft.schemas.exchange.services._2006.messages.ResponseMessageType;
import com.microsoft.schemas.exchange.services._2006.types.BaseFolderType;
import com.microsoft.schemas.exchange.services._2006.types.DefaultShapeNamesType;
import com.microsoft.schemas.exchange.services._2006.types.DistinguishedFolderIdNameType;
import com.microsoft.schemas.exchange.services._2006.types.ResponseClassType;
import com.tryane.saas.connector.exchange.utils.cxf.builder.NonEmptyArrayBuilder;
import com.tryane.saas.connector.exchange.utils.cxf.builder.ResponseShapeBuilder;
import com.tryane.saas.connector.exchange.utils.ews.ExchangeWSConnectionInfos;
import com.tryane.saas.connector.exchange.utils.ews.IExchangeWS;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;

public class ExchangeWebServiceApiRunner extends AbstractSpringRunner {

	private static final Logger	LOGGER	= LoggerFactory.getLogger(ExchangeWebServiceApiRunner.class);

	@Autowired
	private IExchangeWS			exchangeWS;

	private String				DOMAIN	= "tryaneexchange2013.local";

	@Override
	protected void testImplementation() {

		ExchangeWSConnectionInfos exchangeWSConnectionInfos = new ExchangeWSConnectionInfos();
		exchangeWSConnectionInfos.setServerName("https://" + DOMAIN + "/ews/exchange.asmx");
		exchangeWSConnectionInfos.setUserName("tryane");
		exchangeWSConnectionInfos.setPassword("DPDA69cim");

		GetFolderType getFolderRequest = new GetFolderType();
		getFolderRequest.setFolderIds(NonEmptyArrayBuilder.buildFolderIds(DistinguishedFolderIdNameType.DELETEDITEMS));
		getFolderRequest.setFolderShape(ResponseShapeBuilder.buildFolderResponseShape(DefaultShapeNamesType.ID_ONLY));

		Holder<GetFolderResponseType> responseHolder = new Holder<GetFolderResponseType>();
		exchangeWS.getFolder(getFolderRequest, responseHolder, exchangeWSConnectionInfos, "tryane@" + DOMAIN);

		/* Build result */
		List<JAXBElement<? extends ResponseMessageType>> responseMessageList = responseHolder.value.getResponseMessages().getCreateItemResponseMessageOrDeleteItemResponseMessageOrGetItemResponseMessage();
		for (JAXBElement<? extends ResponseMessageType> responseMessage : responseMessageList) {
			if (ResponseClassType.SUCCESS.equals(responseMessage.getValue().getResponseClass())) {
				if (responseMessage.getValue() instanceof FolderInfoResponseMessageType) {
					for (BaseFolderType folder : ((FolderInfoResponseMessageType) responseMessage.getValue()).getFolders().getFolderOrCalendarFolderOrContactsFolder()) {
						//exchangeMailCollaboratorContext.getMailBoxProperties().put(ExchangeCollaboratorContext.PROPERTY_TRASH_DIR_ID, folder.getFolderId().getId());
						//LOGGER.debug("Identified folder {} as Trash folder", exchangeMailCollaboratorContext.getMailBoxProperties().get(ExchangeCollaboratorContext.PROPERTY_TRASH_DIR_ID));
						LOGGER.info("find folder {}", folder.getDisplayName());
					}
				}
			} else {
				LOGGER.error("There was an error while calling Exchange service. {}: {}", responseMessage.getValue().getResponseCode(), responseMessage.getValue().getMessageText());
			}
		}

	}

	public static void main(String[] args) {
		new ExchangeWebServiceApiRunner().runTest("dev", PersonalAppConfig.class);
	}

}
