package com.example.feign.demo;

import org.springframework.context.annotation.Bean;

public class FeignConfigExample {
	
	@Bean
	BeanType specialBean() {
		return new BeanType();
	}

}
