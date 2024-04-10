package org.test.inheritance;

import org.springframework.web.bind.annotation.GetMapping;

public class ControllerAsSubclassAndInterfaceHierarchy extends SuperclassWithMappingPath implements EmptyInterfaceWithinHierarchy {
	
	@GetMapping("last-path-segment")
	public String sayHello() {
		return "hello";
	}

}
