package org.test;

import org.springframework.web.bind.annotation.RequestMapping;

public class ChainedRequestMappingOverMultipleClasses {
	
	@RequestMapping(ChainElement1.MAPPING_PATH_1)
	public String hello() {
		return "Hello";
	}

}
