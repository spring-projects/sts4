package org.test.inheritance;

import org.springframework.web.bind.annotation.RequestMapping;

public class SubclassWithMappingFromParent extends SuperclassWithMappingPath {
	
	@RequestMapping("/")
	public String hello() {
		return "Hello";
	}
	
}
