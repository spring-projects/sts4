package org.test;

import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.*;

import org.springframework.stereotype.Controller;

@Controller
@RequestMapping(value = "parent", method = {GET,POST,DELETE})
public class ParentMappingClass {

	@RequestMapping(value = "/greeting", method = GET)
	public String hello() {
		return "Hello";
	}

}
