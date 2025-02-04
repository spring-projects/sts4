package com.example.events.demo;

import org.springframework.context.ApplicationListener;

public class EventListenerPerInterfaceAndBeanMethod implements ApplicationListener<CustomApplicationEvent> {

	@Override
	public void onApplicationEvent(CustomApplicationEvent event) {
		System.out.println("Event received via listener implementation and bean method: " + event);
	}

}
