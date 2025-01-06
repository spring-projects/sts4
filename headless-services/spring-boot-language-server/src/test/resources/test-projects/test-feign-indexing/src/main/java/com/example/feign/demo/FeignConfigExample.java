package com.example.feign.demo;

import org.springframework.context.annotation.Bean;

public class FeignConfigExample {
	
	@Bean
	public BeanType specialBean() {
		return new BeanType();
	}

}
