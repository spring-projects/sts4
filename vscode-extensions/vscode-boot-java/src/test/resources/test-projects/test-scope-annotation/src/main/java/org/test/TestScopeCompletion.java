package org.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@Scope("onClass")
public class TestScopeCompletion {
	
	@Bean
	@Scope("onMethod")
	public Object myBean() {
		return null;
	}
}
