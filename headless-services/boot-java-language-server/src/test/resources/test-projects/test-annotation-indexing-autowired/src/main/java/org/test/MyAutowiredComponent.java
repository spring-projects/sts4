package org.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MyAutowiredComponent {
	
	private DependencyA depA;
	private DependencyB depB;

	@Autowired
	public MyAutowiredComponent(DependencyA depA, DependencyB depB) {
		this.depA = depA;
		this.depB = depB;
	}
	
	public DependencyA getDepA() {
		return depA;
	}
	
	public DependencyB getDepB() {
		return depB;
	}

}
