package org.test.inheritance;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ControllerAsSubclassAndInterfaceHierarchy extends SuperclassWithMappingPath implements EmptyInterfaceWithinHierarchy {
	
	@GetMapping("last-path-segment")
	public String sayHello() {
		return "hello";
	}

}
