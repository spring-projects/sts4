package org.test.inheritance;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SubclassWithMappingFromParentWithConstant extends SuperclassWithMappingPathFromConstant {
	
	@RequestMapping("/")
	public String hello() {
		return "Hello";
	}
	
}
