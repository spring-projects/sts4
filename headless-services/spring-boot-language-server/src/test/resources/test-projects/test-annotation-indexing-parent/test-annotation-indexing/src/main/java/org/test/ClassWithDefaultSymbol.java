package org.test;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.lang.NonNull;

@Configurable
public class ClassWithDefaultSymbol {
	
	@NonNull
	public String foo() {
		return "foo";
	}
}
