package org.test;

import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("parent")
public class ParentMappingClass {

	@RequestMapping("/greeting")
	public String hello() {
		return "Hello";
	}

}
