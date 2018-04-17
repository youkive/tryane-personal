package com.tryane.saas.personal.sharepoint.manager;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import com.tryane.saas.core.sp.item.SPItem;
import com.tryane.saas.utils.hibernate.ICallBack;
import com.tryane.saas.utils.hibernate.ScrollableResultsProcessor;

@Component
public class PersonalSPItemManager implements IPersonalSPItemManager {

	@PersistenceContext(unitName = "saas-data")
	private EntityManager entityManager;

	@Override
	public void processAllItems(ICallBack<SPItem> callback) {
		ScrollableResultsProcessor<SPItem> scrollableResultsProcessor = new ScrollableResultsProcessor<>(((Session) entityManager.getDelegate()).getSessionFactory(), 1000);
		scrollableResultsProcessor.createQuery("FROM SPItem");
		scrollableResultsProcessor.processObjects(callback);
	}

}
