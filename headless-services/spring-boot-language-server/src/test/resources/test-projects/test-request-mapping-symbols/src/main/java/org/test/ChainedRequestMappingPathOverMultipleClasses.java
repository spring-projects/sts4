package org.test;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ChainedRequestMappingPathOverMultipleClasses {
	
	@RequestMapping(ChainElement1.MAPPING_PATH_1)
	public String hello() {
		return "Hello";
	}

}
