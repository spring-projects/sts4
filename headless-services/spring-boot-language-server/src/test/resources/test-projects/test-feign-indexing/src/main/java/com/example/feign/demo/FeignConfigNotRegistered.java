package com.example.feign.demo;

import org.springframework.context.annotation.Bean;

public class FeignConfigNotRegistered {
	
	@Bean
	BeanType specialBean2() {
		return new BeanType();
	}

}
