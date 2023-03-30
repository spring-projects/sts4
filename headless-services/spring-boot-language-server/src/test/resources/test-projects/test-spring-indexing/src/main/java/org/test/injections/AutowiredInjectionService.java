package org.test.injections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.test.BeanClass1;
import org.test.BeanClass2;

@Service
public class AutowiredInjectionService {

	@Autowired private BeanClass1 bean1;
	@Autowired private BeanClass2 bean2;
	
	public BeanClass1 getBean1() {
		return bean1;
	}
	
	public BeanClass2 getBean2() {
		return bean2;
	}

}
