package com.example.configproperties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "com.example.config.record.prefix2")
public record ConfigurationPropertiesWithRecordsAndConfigurationAnnotation (String name, int duration) {}
