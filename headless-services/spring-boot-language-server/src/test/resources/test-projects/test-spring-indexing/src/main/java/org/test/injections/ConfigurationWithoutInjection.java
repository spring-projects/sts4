package org.test.injections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.test.BeanClass1;

@Configuration
public class ConfigurationWithoutInjection {

	@Bean
	BeanClass1 beanWithoutInjections() {
		return new BeanClass1();
	}

}
