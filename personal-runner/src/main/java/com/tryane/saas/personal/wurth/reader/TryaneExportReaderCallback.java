package com.tryane.saas.personal.wurth.reader;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tryane.saas.connector.graph.utils.api.reports.IGraphReportReaderCallback;
import com.tryane.saas.personal.wurth.item.TryaneExportUser;

public class TryaneExportReaderCallback implements IGraphReportReaderCallback<TryaneExportUser> {

	private static final Logger		LOGGER	= LoggerFactory.getLogger(TryaneExportReaderCallback.class);

	private Set<TryaneExportUser>	usersInTryane;

	private Set<TryaneExportUser>	adminInTryane;

	public TryaneExportReaderCallback() {
		usersInTryane = new HashSet<>();
		adminInTryane = new HashSet<>();
	}

	@Override
	public void processObject(TryaneExportUser item) {
		if (item.getIsAdmin()) {
			adminInTryane.add(item);
		} else if (!usersInTryane.add(item)) {
			LOGGER.warn("doublon : {}", item.getSpId());
		}
	}

	@Override
	public void finish() {
		Set<TryaneExportUser> finalAdminsDoublon = new HashSet<>();
		this.adminInTryane.forEach(user -> {
			if (!usersInTryane.add(user)) {
				finalAdminsDoublon.add(user);
			}
		});
		adminInTryane = finalAdminsDoublon;
	}

	public Set<TryaneExportUser> getUsersInTryane() {
		return usersInTryane;
	}

	public Set<TryaneExportUser> getAdminInTryane() {
		return adminInTryane;
	}

}
