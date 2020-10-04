package com.tryane.saas.toolsv2.infra;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.tryane.saas.toolsv2.domain.client.Client;
import com.tryane.saas.toolsv2.domain.client.IClientsService;

@Repository
public class ClientsRepository implements IClientsService {

	private IClientDao clientDao;

	@Autowired
	public ClientsRepository(IClientDao clientDao) {
		this.clientDao = clientDao;
	}

	@Override
	public List<Client> getAllClients() {
		List<ClientEntity> allCLients = clientDao.getAll();
		return allCLients.stream().map(client -> new Client(client.getName())).collect(Collectors.toList());
	}

}
