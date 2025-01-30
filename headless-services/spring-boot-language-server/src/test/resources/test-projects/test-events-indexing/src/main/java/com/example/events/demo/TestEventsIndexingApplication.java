package com.example.events.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TestEventsIndexingApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestEventsIndexingApplication.class, args);
	}
	
	@Bean
	public EventListenerPerInterfaceAndBeanMethod listenerBean() {
		return new EventListenerPerInterfaceAndBeanMethod();
	}

}
