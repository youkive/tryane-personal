package com.tryane.saas.personal.model.viewer.model;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tryane.saas.core.client.IClientManager;

@Component
@Path("/clients")
public class ClientsController {

	@Autowired
	private IClientManager clientManager;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ClientWrapper> getClients() {
		return clientManager.getAllClients().stream().map(client -> {
			return new ClientWrapper(client);
		}).collect(Collectors.toList());
	}
}
