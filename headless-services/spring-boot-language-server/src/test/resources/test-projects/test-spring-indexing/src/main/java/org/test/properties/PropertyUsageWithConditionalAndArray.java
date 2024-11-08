package org.test.properties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = {"my.prop2"})
public class PropertyUsageWithConditionalAndArray {
	
}