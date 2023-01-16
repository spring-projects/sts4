package com.record.props;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * User properties.
 */
@ConfigurationProperties("application.temp.user")
public record UserProperties(String name, String password, List<String> roles) {

}