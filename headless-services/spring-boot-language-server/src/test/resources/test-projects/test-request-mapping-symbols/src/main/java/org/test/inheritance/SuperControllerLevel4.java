package org.test.inheritance;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SuperControllerLevel4 extends SuperControllerLevel3 {
	
	@RequestMapping("final-subclass-path")
	public String details() {
		return "subclass-details";
	}

}
