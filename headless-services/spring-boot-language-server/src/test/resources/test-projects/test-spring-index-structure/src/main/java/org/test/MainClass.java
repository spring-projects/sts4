package org.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MainClass {
	
	@Value("server.port")
	private String serverport;
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(MainClass.class, args);
	}
	
	@Bean
	BeanClass1 bean1() {
		return new BeanClass1();
	}
	
}
