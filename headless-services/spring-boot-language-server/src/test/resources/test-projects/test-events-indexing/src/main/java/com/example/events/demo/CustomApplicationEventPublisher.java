package com.example.events.demo;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class CustomApplicationEventPublisher {
	
	private ApplicationEventPublisher publisher;

	public CustomApplicationEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}
	
	public void foo() {
		this.publisher.publishEvent(new CustomApplicationEvent(null));
	}

}
