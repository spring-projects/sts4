package org.test;

import org.springframework.beans.factory.annotation.Value;

public class TestValueCompletion {
	
	@Value("onField")
	private String value1;
	
	@Value("onMethod")
	public void method1() {
	}
	
	public void method2(@Value("onParameter") String parameter1) {
	}
	
}
