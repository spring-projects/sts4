package com.example.demo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "justauth")
public class JustAuthProperties {
	
	private String name;
	
    private Map<String, AuthConfig> type = new HashMap<>();

	public Map<String, AuthConfig> getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

