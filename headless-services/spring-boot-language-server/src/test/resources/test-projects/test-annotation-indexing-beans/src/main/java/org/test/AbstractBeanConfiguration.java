package org.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public abstract class AbstractBeanConfiguration {
	
	@Bean
	public abstract BeanClass abstractBean();

}
