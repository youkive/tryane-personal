package com.tryane.saas.personal.sharepoint;

import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tryane.saas.core.network.properties.INetworkPropertyManager;
import com.tryane.saas.core.network.properties.NetworkProperty;

public class NetworkPropertyManagerMock implements INetworkPropertyManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(NetworkPropertyManagerMock.class);

	@Override
	public String getNetworkPropertyValue(String networkId, String propertyName) {
		LOGGER.info("WAAFFF");
		return null;
	}

	@Override
	public <T> T getNetworkPropertyValue(String networkId, String propertyName, Function<String, T> converter, T defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetworkProperty getNetworkProperty(String networkId, String propertyName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNetworkPropertyValue(String networkId, String propertyName, Object propertyValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteNetworkProperty(String networkId, String propertyName) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<NetworkProperty> getAllNetworksProperties(String... propertyNames) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<NetworkProperty> getNetworkProperties(String networkId, String... networkProps) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<NetworkProperty> getNetworkProperties(String networkId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNetworkPropertyValueInCacheOnly(String networkId, String propertyName, Object propertyValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteAllNetworkProperties(String networkId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateNetworkPropertyValue(NetworkProperty networkProperty) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<NetworkProperty> findNetworkPropertiesWithNameAndValue(String propertyName, String value) {
		// TODO Auto-generated method stub
		return null;
	}

}
