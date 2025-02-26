package org.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingConstantRequestMapping {

	public final static String PING = "/ping";
	
	@GetMapping(PongConstantRequestMapping.PONG)
	public String hello() {
		return "oy?";
	}

	
}
