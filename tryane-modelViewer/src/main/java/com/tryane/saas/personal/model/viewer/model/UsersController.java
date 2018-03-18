package com.tryane.saas.personal.model.viewer.model;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tryane.saas.core.user.User;
import com.tryane.saas.personal.model.viewer.manager.IUserManagerExtended;

@Component
@Path("/users")
public class UsersController {

	@Autowired
	private IUserManagerExtended userManager;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<User> getUsers() {
		return userManager.getAllUsers();
	}
}
