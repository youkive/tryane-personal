package com.tryane.saas.personal.amersports;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tryane.saas.utils.crypto.AesCryptoManager;
import com.tryane.saas.utils.crypto.ICryptoManager;
import com.tryane.saas.utils.jackson.JacksonUtils;

public class CustomListPropertyInsertRunner {

	private static final String	NETWORK_ID		= "s1";

	public static final String	PROPERTY_KEY	= "onpremise.sharepoint.listtemplate.custom";

	public static void main(String[] args) {

		ICryptoManager cryptoManager = new AesCryptoManager();
		StringBuilder requestBuilder = new StringBuilder();

		requestBuilder.append("INSERT INTO core_networkprop(networkid,name,value)  values");
		requestBuilder.append("('").append(NETWORK_ID);
		requestBuilder.append("','").append(PROPERTY_KEY).append("'");

		ObjectNode customListProperty = JacksonUtils.MAPPER.createObjectNode();
		customListProperty.put("10004", "DOC");
		customListProperty.put("10005", "DOC");
		//customListProperty.put("10001", "IGNORED");
		//customListProperty.put("10002", "IGNORED");
		//customListProperty.put("10003", "IGNORED");
		//customListProperty.put("10007", "IGNORED");
		//customListProperty.put("10102", "IGNORED");

		requestBuilder.append(",decode('").append(bytesToHex(cryptoManager.encrypt(customListProperty.toString()))).append("', 'hex')");
		requestBuilder.append(");");
		System.out.println(requestBuilder.toString());

		requestBuilder = new StringBuilder();
		requestBuilder.append("UPDATE core_networkprop set value=");
		requestBuilder.append("decode('").append(bytesToHex(cryptoManager.encrypt(customListProperty.toString()))).append("', 'hex')");
		requestBuilder.append(" where networkid='").append(NETWORK_ID).append("' and name='").append(PROPERTY_KEY).append("';");
		System.out.println(requestBuilder.toString());
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
