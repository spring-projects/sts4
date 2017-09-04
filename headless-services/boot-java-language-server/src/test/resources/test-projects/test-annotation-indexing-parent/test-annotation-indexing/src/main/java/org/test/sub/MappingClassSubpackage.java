package org.test.sub;

import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/classlevel")
public class MappingClassSubpackage {
	
	@RequestMapping("mapping-subpackage")
	public String hello1() {
		return "hello1";
	}

}
