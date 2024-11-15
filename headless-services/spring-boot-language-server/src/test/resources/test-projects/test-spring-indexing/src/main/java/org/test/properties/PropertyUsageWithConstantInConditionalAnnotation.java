package org.test.properties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(Constants.PURE_PROPERTY_VALUE)
public class PropertyUsageWithConstantInConditionalAnnotation {
	
}
