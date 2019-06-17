package org.test;

import org.springframework.web.bind.annotation.GetMapping;

public class PongConstantRequestMapping {

	public static final String PONG="/pong";
	
	@GetMapping(PingConstantRequestMapping.PING)
	public String hello() {
		return "oy?";
	}

}
