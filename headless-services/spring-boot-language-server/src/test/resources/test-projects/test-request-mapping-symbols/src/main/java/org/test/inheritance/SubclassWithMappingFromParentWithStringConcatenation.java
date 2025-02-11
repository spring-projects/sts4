package org.test.inheritance;

import org.springframework.web.bind.annotation.RequestMapping;

public class SubclassWithMappingFromParentWithStringConcatenation extends SuperclassWithMappingPathWithStringConcatenation {
	
	@RequestMapping("/sub" + "class")
	public String hello() {
		return "Hello";
	}
	
}
