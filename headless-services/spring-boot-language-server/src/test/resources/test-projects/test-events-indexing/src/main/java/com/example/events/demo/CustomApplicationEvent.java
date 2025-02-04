package com.example.events.demo;

import org.springframework.context.ApplicationEvent;

public class CustomApplicationEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	public CustomApplicationEvent(Object source) {
		super(source);
	}

}
