package org.test;

public class ManuallyCreatedBeanWithConstructor {
	
	private BeanClass1 bean1;
	private BeanClass2 bean2;
	
	public ManuallyCreatedBeanWithConstructor(BeanClass1 bean1, BeanClass2 bean2) {
		this.bean1 = bean1;
		this.bean2 = bean2;
	}
	
	public BeanClass1 getBean1() {
		return bean1;
	}
	
	public BeanClass2 getBean2() {
		return bean2;
	}

}
