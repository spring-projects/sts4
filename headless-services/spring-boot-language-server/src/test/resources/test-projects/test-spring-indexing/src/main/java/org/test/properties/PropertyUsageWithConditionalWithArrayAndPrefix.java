package org.test.properties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "my", name = {"prop2"})
public class PropertyUsageWithConditionalWithArrayAndPrefix {
	
}
