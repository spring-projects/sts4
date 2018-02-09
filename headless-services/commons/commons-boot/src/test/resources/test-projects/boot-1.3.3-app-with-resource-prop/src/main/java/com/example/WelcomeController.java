package com.example;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeController {
	
	@Autowired
	private WelcomeProperties welcome;

	@RequestMapping(value="/", produces="text/plain")
	public Resource welcome() {
		return welcome.getResource();
	}
	
	/**
	 * Shows some debug infos.
	 */
	@RequestMapping("/debug")
	public String debug() throws IOException {
		Resource rsrc = welcome.getResource();
		return rsrc.getURI().toString();
	}

}
