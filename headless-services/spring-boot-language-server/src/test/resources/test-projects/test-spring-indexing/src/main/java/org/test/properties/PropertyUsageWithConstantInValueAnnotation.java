package org.test.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PropertyUsageWithConstantInValueAnnotation {
	
	@Value(Constants.EMBEDDED_PROPERTY_VALUE)
	private String someProp;

}
