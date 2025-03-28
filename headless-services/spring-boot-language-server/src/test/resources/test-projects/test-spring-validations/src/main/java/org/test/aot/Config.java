package org.test.aot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
	
	@Bean
	RegistetedViaConfigBeanRegistrationAotProcessor registeredViaConfigAotProcessor() {
		return new RegistetedViaConfigBeanRegistrationAotProcessor();
	}

}
