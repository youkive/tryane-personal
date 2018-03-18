package com.tryane.saas.personal.model.viewer.manager;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;

import com.tryane.saas.core.user.User;
import com.tryane.saas.core.user.UserManager;

@Component
public class UserManagerExtended extends UserManager implements IUserManagerExtended {

	@PersistenceContext(unitName = "saas-client-data")
	private EntityManager entityManager;

	@SuppressWarnings("unchecked")
	@Override
	public List<User> getAllUsers() {
		return entityManager.createQuery("from User").getResultList();
	}

}
