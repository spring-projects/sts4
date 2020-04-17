package com.example.demo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("myprops")
public class TestProperties {

	private List<NestedProperties> nested = new ArrayList<>();

	public List<NestedProperties> getNested() {
		return nested;
	}

	public void setNested(List<NestedProperties> nested) {
		this.nested = nested;
	}

	public static class NestedProperties extends SuperclassProperties {

		private String foo;

		public String getFoo() {
			return foo;
		}

		public void setFoo(String foo) {
			this.foo = foo;
		}

	}

	public static class SuperclassProperties {

		private String bar;

		public String getBar() {
			return bar;
		}

		public void setBar(String bar) {
			this.bar = bar;
		}

	}

}