package org.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpecialConfiguration {
	
	@Bean("implicitNamedBean")
	public BeanClass specialBean() {
		return new BeanClass();
	}

	@Bean(value="valueBean")
	public BeanClass specialBean2() {
		return new BeanClass();
	}

	@Bean(value= {"valueBean1", "valueBean2"})
	public BeanClass specialBean3() {
		return new BeanClass();
	}

	@Bean(name="namedBean")
	public BeanClass specialBean4() {
		return new BeanClass();
	}

	@Bean(name= {"namedBean1", "namedBean2"})
	public BeanClass specialBean5() {
		return new BeanClass();
	}

}
