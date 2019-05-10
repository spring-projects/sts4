package org.springframework.ide.vscode.concourse;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class LogTestStartAndEnd extends TestWatcher {
	
	@Override
	protected void starting(Description description) {
		System.out.println(">>>> starting test: "+description.getClassName()+" . "+description.getMethodName());
	}

	@Override
	protected void finished(Description description) {
		System.out.println("<<<< finished test: "+description.getClassName()+" . "+description.getMethodName());
	}
}
