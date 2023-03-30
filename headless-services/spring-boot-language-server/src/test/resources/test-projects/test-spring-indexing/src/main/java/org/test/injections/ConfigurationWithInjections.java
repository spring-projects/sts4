package org.test.injections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.test.BeanClass1;
import org.test.BeanClass2;
import org.test.ManuallyCreatedBeanWithConstructor;

@Configuration
public class ConfigurationWithInjections {

	@Bean
	ManuallyCreatedBeanWithConstructor manualBeanWithConstructor(BeanClass1 bean1, BeanClass2 bean2) {
		return new ManuallyCreatedBeanWithConstructor(bean1, bean2);
	}

}
