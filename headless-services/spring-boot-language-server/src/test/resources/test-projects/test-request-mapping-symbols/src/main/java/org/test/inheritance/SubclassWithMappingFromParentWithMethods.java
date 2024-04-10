package org.test.inheritance;

import org.springframework.web.bind.annotation.RequestMapping;

public class SubclassWithMappingFromParentWithMethods extends SuperclassWithMappingPathAndMethods {
	
	@RequestMapping
	public String hello() {
		return "Hello";
	}
	
}
