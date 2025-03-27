package org.test;

import org.springframework.context.annotation.Bean;

public class BeanMethodsWithoutConfiguration {
	
	@Bean
	public BeanClass1 beanWithoutConfig() {
		return new BeanClass1(); 
	}
	
	@Bean
	public BeanClass2 beanWithoutConfig2() {
		return new BeanClass2();
	}

}
