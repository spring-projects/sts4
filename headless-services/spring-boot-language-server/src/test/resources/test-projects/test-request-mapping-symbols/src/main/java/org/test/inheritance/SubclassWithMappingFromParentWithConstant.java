package org.test.inheritance;

import org.springframework.web.bind.annotation.RequestMapping;

public class SubclassWithMappingFromParentWithConstant extends SuperclassWithMappingPathFromConstant {
	
	@RequestMapping("/")
	public String hello() {
		return "Hello";
	}
	
}
