package org.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PongConstantRequestMapping {

	public static final String PONG="/pong";
	
	@GetMapping(PingConstantRequestMapping.PING)
	public String hello() {
		return "oy?";
	}

}
