package org.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimpleConfiguration {
	
	@Bean()
	public BeanClass simpleBean() {
		return new BeanClass();
	}
}
