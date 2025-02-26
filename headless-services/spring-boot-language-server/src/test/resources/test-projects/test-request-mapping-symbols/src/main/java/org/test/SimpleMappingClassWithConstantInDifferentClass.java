package org.test;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SimpleMappingClassWithConstantInDifferentClass {
	
	@RequestMapping(Constants.REQUEST_MAPPING_PATH)
	public String hello() {
		return "Hello";
	}

}
