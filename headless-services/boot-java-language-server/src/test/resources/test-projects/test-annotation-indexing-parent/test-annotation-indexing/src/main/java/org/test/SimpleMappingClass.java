package org.test;

import org.springframework.web.bind.annotation.RequestMapping;

public class SimpleMappingClass {
	
	@RequestMapping("mapping1")
	public String hello1() {
		return "hello1";
	}

	@RequestMapping("mapping2")
	public String hello2() {
		return "hello2";
	}

}
