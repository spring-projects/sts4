package com.example.events.demo;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "com.example.config.record.prefix")
public record ConfigurationPropertiesWithRecords (String name, int duration) {}
