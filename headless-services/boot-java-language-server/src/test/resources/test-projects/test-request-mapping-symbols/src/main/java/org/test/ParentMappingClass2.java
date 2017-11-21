package org.test;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod.*;

@RequestMapping(value="parent2", method= {GET,POST,DELETE})
public class ParentMappingClass2 {

	@RequestMapping
	public void nothing() {}
	
}
