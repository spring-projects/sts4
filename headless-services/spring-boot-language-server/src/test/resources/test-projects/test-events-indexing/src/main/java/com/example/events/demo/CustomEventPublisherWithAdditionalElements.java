package com.example.events.demo;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Qualifier("qualifier")
@Component
public class CustomEventPublisherWithAdditionalElements {
	
	private ApplicationEventPublisher publisher;

	public CustomEventPublisherWithAdditionalElements(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}
	
	public void foo() {
		this.publisher.publishEvent(new CustomEvent());
	}

}
