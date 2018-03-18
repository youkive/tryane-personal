package com.tryane.saas.personal.model.viewer.manager;

import java.util.List;

import com.tryane.saas.core.user.IUserManager;
import com.tryane.saas.core.user.User;

public interface IUserManagerExtended extends IUserManager {

	public List<User> getAllUsers();
}
