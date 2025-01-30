package com.example.events.demo;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class CustomEventPublisher {
	
	private ApplicationEventPublisher publisher;

	public CustomEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}
	
	public void foo() {
		this.publisher.publishEvent(new CustomEvent());
	}

}
