package org.test.inheritance;

import org.springframework.web.bind.annotation.RequestMapping;

public class SuperControllerLevel4 extends SuperControllerLevel3 {
	
	@RequestMapping("final-subclass-path")
	public String details() {
		return "subclass-details";
	}

}
