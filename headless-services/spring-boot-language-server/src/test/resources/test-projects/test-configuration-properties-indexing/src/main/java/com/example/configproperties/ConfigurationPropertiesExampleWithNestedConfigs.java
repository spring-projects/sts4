package com.example.configproperties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "com.example.config.prefix.nested")
public class ConfigurationPropertiesExampleWithNestedConfigs {
	
	private final String parentLevelConfig;
	private final ConfigBundle bundle;
	
	public ConfigurationPropertiesExampleWithNestedConfigs(String parentLevelConfig, ConfigBundle bundle) {
		super();
		this.parentLevelConfig = parentLevelConfig;
		this.bundle = bundle;
	}

	public ConfigBundle getBundle() {
		return bundle;
	}
	
	public String getParentLevelConfig() {
		return parentLevelConfig;
	}
	
}
