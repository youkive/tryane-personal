package com.tryane.saas.personal.wurth.reader;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tryane.saas.connector.graph.utils.api.reports.IGraphReportReaderCallback;
import com.tryane.saas.personal.wurth.item.WurthADUser;

public class WurthADReaderCallback implements IGraphReportReaderCallback<WurthADUser> {

	private static final Logger	LOGGER	= LoggerFactory.getLogger(WurthADReaderCallback.class);

	private Set<WurthADUser>	usersIdentifiersAD;

	public WurthADReaderCallback() {
		usersIdentifiersAD = new HashSet<>();
	}

	@Override
	public void processObject(WurthADUser item) {
		if (!usersIdentifiersAD.add(item)) {
			LOGGER.warn("doublon : {}", item.getId());
		}
	}

	@Override
	public void finish() {
	}

	public Set<WurthADUser> getUsersIdentifiersAD() {
		return usersIdentifiersAD;
	}

}
