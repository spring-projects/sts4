package org.test;

import org.springframework.web.bind.annotation.RequestMapping;

public class ChainedRequestMappingPathOverMultipleClasses {
	
	@RequestMapping(ChainElement1.MAPPING_PATH_1)
	public String hello() {
		return "Hello";
	}

}
