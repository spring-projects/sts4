package org.test;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.support.AbstractBeanDefinition;

public class SimpleMappingClassWithConstantFromBinaryType {
	
	@RequestMapping(AbstractBeanDefinition.INFER_METHOD)
	public String hello() {
		return "Hello";
	}

}
