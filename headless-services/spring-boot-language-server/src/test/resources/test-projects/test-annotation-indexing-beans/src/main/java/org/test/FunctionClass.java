package org.test;

import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FunctionClass {
	
	@Bean
	public Function<String, String> uppercase() {
		return str -> str.toUpperCase();
	}

}
