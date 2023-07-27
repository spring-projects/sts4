package org.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanMethodNotPublic {

	@Bean
	public BeanClass1 publicBeanMethod() {
		return new BeanClass1();
	}
	
	@Bean
	BeanClass2 nonPublicBeanMethod() {
		return new BeanClass2();
	}

}
