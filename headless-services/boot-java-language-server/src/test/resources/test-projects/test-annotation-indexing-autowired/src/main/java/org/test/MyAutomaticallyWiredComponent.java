package org.test;

import org.springframework.stereotype.Component;

@Component
public class MyAutomaticallyWiredComponent {

	private DependencyA depA;
	private DependencyB depB;

	public MyAutomaticallyWiredComponent(DependencyA depA, DependencyB depB) {
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
