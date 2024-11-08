package org.test.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PropertyUsageWithValue {
	
	@Value("${my.prop2}")
	private String someProp;
	
}