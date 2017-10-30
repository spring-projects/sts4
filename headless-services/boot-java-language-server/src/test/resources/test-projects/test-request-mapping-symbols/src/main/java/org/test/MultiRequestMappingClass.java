package org.test;

import org.springframework.web.bind.annotation.RequestMapping;

public class MultiRequestMappingClass {
	
	@RequestMapping(path= {"/hello1","hello2"})
	public String hello() {
		return "Hello!!!";
	}

}
