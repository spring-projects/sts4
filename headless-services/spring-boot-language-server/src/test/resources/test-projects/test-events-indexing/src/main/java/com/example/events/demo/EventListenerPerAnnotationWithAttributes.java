package com.example.events.demo;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EventListenerPerAnnotationWithAttributes {
	
	@EventListener({ApplicationReadyEvent.class})
	public void handleEvent(ApplicationEvent event) {
		System.out.println("Specialized application ready listener: " + event);
	}

}
