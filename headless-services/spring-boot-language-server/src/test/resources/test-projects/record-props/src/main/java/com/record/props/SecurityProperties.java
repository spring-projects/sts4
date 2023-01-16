package com.record.props;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("application.security")
public record SecurityProperties(List<UserProperties> users) {
	
}