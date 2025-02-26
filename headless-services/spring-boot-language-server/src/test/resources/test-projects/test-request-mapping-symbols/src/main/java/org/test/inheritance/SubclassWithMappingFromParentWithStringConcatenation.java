package org.test.inheritance;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SubclassWithMappingFromParentWithStringConcatenation extends SuperclassWithMappingPathWithStringConcatenation {
	
	@RequestMapping("/sub" + "class")
	public String hello() {
		return "Hello";
	}
	
}
