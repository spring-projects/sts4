package com.example.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("my")
public class MyProps {

	private Properties props = new Properties();
	
	private Map<String, Person> map = new HashMap<>();

	public Map<String, Person> getMap() {
		return map;
	}

	public Properties getProps() {
		return props;
	}
	
}
