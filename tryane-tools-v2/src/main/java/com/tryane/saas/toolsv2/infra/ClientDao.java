package com.tryane.saas.toolsv2.infra;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;

@Component
public class ClientDao implements IClientDao {

	@PersistenceContext(unitName = "saas-client-data")
	private EntityManager entityManager;

	@SuppressWarnings("unchecked")
	@Override
	public List<ClientEntity> getAll() {
		return entityManager.createQuery("from ClientEntity").getResultList();
	}

}
