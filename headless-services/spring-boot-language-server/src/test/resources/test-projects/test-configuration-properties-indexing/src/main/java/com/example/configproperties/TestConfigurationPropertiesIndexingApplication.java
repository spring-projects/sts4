package com.example.configproperties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan({ "com.example"})
public class TestConfigurationPropertiesIndexingApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestConfigurationPropertiesIndexingApplication.class, args);
	}
	
}
