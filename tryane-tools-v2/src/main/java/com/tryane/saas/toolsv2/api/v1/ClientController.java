package com.tryane.saas.toolsv2.api.v1;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;

import com.tryane.saas.toolsv2.domain.client.Client;
import com.tryane.saas.toolsv2.domain.client.IClientsService;

@Path("/clients")
public class ClientController {

	private IClientsService clientsService;

	@Autowired
	public ClientController(IClientsService clientsService) {
		this.clientsService = clientsService;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Client> getAllClients() {
		return clientsService.getAllClients();
	}
}
