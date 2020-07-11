package org.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;

public class TestValueCompletion {
	
	@Value("onField")
	private String value1;
	
	@Value("onMethod")
	public void method1() {
	}
	
	public void method2(@Value("onParameter") String parameter1) {
	}
	
}
