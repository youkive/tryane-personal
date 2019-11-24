package com.tryane.saas.modelviewer.controller;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tryane.saas.modelviewer.dao.common.UserManager;
import com.tryane.saas.modelviewer.model.common.User;

@Component
@Path("/users")
public class UsersController {

	@Autowired
	private UserManager userManager;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<User> essai() {
		return userManager.findAll();
	}
}
