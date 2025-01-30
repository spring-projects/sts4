package com.example.events.demo;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

public class EventListenerPerInterfaceAndBeanMethod implements ApplicationListener<ApplicationEvent> {

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		System.out.println("Event received via listener implementation and bean method: " + event);
	}

}
