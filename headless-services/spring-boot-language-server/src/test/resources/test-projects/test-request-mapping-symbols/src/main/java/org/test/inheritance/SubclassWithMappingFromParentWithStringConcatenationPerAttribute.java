package org.test.inheritance;

import org.springframework.web.bind.annotation.RequestMapping;

public class SubclassWithMappingFromParentWithStringConcatenationPerAttribute extends SuperclassWithMappingPathWithStringConcatenationPerAttribute {
	
	@RequestMapping(value = "/sub" + "class")
	public String hello() {
		return "Hello";
	}
	
}
