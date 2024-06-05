package org.test.injections;

import org.test.MainClass;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.beans.factory.annotation.Qualifier;
import org.test.BeanClass1;
import org.test.BeanClass2;
import org.test.ManuallyCreatedBeanWithConstructor;

@Configuration
@Qualifier("qualifier")
@ImportRuntimeHints(MainClass.class)
public class ConfigurationWithInjectionsAndAnnotations {

	@Bean
	@DependsOn({"bean1", "bean2"})
	ManuallyCreatedBeanWithConstructor beanWithAnnotationsOnInjectionPoints(@Qualifier("q1") BeanClass1 bean1, @Qualifier("q2") BeanClass2 bean2) {
		return new ManuallyCreatedBeanWithConstructor(bean1, bean2);
	}

}
