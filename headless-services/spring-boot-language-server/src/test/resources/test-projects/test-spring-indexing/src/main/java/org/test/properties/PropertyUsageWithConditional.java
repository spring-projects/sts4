package org.test.properties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty("my.prop2")
public class PropertyUsageWithConditional {
	
}