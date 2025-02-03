package com.example.events.demo;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpecializedCustomEventPublisher {
	
	private ApplicationEventPublisher publisher;

	public SpecializedCustomEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}
	
	public void foo() {
		this.publisher.publishEvent(new SpecializedCustomEvent());
	}

}
