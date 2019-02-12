package com.tryane.saas.personal;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tryane.saas.results.ComputationOptions;
import com.tryane.saas.utils.crypto.AesCryptoManager;
import com.tryane.saas.utils.crypto.ICryptoManager;
import com.tryane.saas.utils.jackson.JacksonUtils;

public class NetworkPropertyUpdater {

	public static final Logger	LOGGER			= LoggerFactory.getLogger(NetworkPropertyUpdater.class);

	public static final String	NETWORK_ID		= "k1";

	// sharepoint.injectjs.connector.data
	public static final String	VALUE			= "{\"sources\":[\"CSV\"],\"csvSourceEnabledforCollaboratorModel\":true,\"csvFilePath\":\"d:/tryane/import_collab/import_collab_skype.csv\",\"csvCollabIdKeyHeaderName\":\"nni\",\"csvEmailHeaderName\":\"email\",\"csvDisplayNameHeaderName\":\"name\",\"csvPrefixLoginSp\":null,\"csvCollabIdKeyType\":\"SHAREPOINT_ID\"}";

	//	public static final String	VALUE			= "";

	public static final String	PROPERTY_KEY	= "connector.configuration";

	public static void main(String[] args) {
		ICryptoManager cryptoManager = new AesCryptoManager();
		StringBuilder requestBuilder = new StringBuilder();

		requestBuilder.append("INSERT INTO core_networkprop(networkid,name,value)  values");
		requestBuilder.append("('").append(NETWORK_ID);
		requestBuilder.append("','").append(PROPERTY_KEY).append("'");
		requestBuilder.append(",decode('").append(bytesToHex(cryptoManager.encrypt(VALUE))).append("', 'hex')");
		requestBuilder.append(");");
		System.out.println(requestBuilder.toString());

		requestBuilder = new StringBuilder();
		requestBuilder.append("UPDATE core_networkprop set value=");
		requestBuilder.append("decode('").append(bytesToHex(cryptoManager.encrypt(VALUE))).append("', 'hex')");
		requestBuilder.append(" where networkid='").append(NETWORK_ID).append("' and name='").append(PROPERTY_KEY).append("';");
		System.out.println(requestBuilder.toString());
	}

	public static String getComputationOption() {
		ComputationOptions computationOptions = new ComputationOptions();
		computationOptions.setRecomputeStartDate(LocalDate.parse("2018-12-27"));
		try {
			return JacksonUtils.MAPPER.writeValueAsString(computationOptions);
		} catch (JsonProcessingException e) {
			LOGGER.error("", e);
			throw new RuntimeException();
		}
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}
