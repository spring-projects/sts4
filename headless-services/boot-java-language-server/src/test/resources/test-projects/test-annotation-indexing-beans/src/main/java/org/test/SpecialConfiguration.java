package org.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpecialConfiguration {
	
	@Bean("namedBean")
	public BeanClass specialBean() {
		return new BeanClass();
	}
	
	@Bean(name= {"beanName1","beanName2"})
	public BeanClass beanWithAlias() {
		return new BeanClass();
	}



}
