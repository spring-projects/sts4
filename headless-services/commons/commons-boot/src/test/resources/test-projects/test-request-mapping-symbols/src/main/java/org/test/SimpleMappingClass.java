package org.test;

import org.springframework.web.bind.annotation.RequestMapping;

public class SimpleMappingClass {
	
	@RequestMapping("/greeting")
	public String hello() {
		return "Hello";
	}

}
