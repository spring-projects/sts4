package org.test;

import org.springframework.web.bind.annotation.RequestMapping;

public class SimpleMappingClassWithConstantInDifferentClass {
	
	@RequestMapping(Constants.REQUEST_MAPPING_PATH)
	public String hello() {
		return "Hello";
	}

}
