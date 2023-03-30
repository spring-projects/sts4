package org.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MainClass {
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(MainClass.class, args);
	}
	
	@Bean
	BeanClass1 bean1() {
		return new BeanClass1();
	}
	
	@Bean
	BeanClass2 bean2() {
		return new BeanClass2();
	}

}
