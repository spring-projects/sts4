package com.example.events.demo;

import org.springframework.boot.CommandLineRunner;
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
	
	@Bean
	public CommandLineRunner runner(CustomEventPublisher pub, SpecializedCustomEventPublisher specialPub) {
		return new CommandLineRunner() {
			
			@Override
			public void run(String... args) throws Exception {
				System.out.println("RUN!!!");
				pub.foo();
				specialPub.foo();
			}
		};
	}

}
