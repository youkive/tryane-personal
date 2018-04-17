package com.tryane.saas.personal.sharepoint.manager;

import com.tryane.saas.core.sp.item.SPItem;
import com.tryane.saas.utils.hibernate.ICallBack;

public interface IPersonalSPItemManager {

	void processAllItems(ICallBack<SPItem> callback);
}
