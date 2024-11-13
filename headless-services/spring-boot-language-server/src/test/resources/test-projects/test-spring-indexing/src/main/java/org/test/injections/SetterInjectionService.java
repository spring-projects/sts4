package org.test.injections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.test.BeanClass1;
import org.test.BeanClass2;

@Service
public class SetterInjectionService {
	
	private BeanClass1 bean1;
	private BeanClass2 bean2;
	
	private BeanClass1 somethingElse;

	public SetterInjectionService() {
	}

	@Autowired
	@Qualifier("setter-injection-qualifier")
	public void setBean1(BeanClass1 bean1) {
		this.bean1 = bean1;
	}
	
	@Autowired
	public void setBean2(@Qualifier("setter-injection-qualifier-on-param") BeanClass2 bean2) {
		this.bean2 = bean2;
	}
	
	public void setSomethingElse(BeanClass1 somethingElseNotInjected) {
		this.somethingElse = somethingElseNotInjected;
	}
	
}
