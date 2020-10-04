package com.tryane.saas.toolsv2.api.v1;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.springframework.stereotype.Component;

@Component
@Path("/test")
public class TestController {

	@GET
	public String test() {
		return "test";
	}
}
