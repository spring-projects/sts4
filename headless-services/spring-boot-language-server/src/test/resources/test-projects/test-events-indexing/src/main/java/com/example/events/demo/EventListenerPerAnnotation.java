package com.example.events.demo;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EventListenerPerAnnotation {
	
	@EventListener
	public void handleEvent(ApplicationEvent event) {
		System.out.println(event);
	}

}
