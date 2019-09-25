/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.project.harness;

import java.util.Set;

import org.springframework.ide.vscode.boot.java.livehover.v2.LiveRequestMapping;

import com.google.common.collect.ImmutableSet;

public class MockRequestMapping implements LiveRequestMapping {

	private String[] paths = {};
	private String className;
	private String methodName;
	private String[] methodParams;
	private Set<String> requestMethods = ImmutableSet.of();

	@Override
	public String[] getSplitPath() {
		return paths;
	}

	@Override
	public String getFullyQualifiedClassName() {
		return className;
	}

	@Override
	public String getMethodName() {
		return methodName;
	}

	@Override
	public String[] getMethodParameters() {
		return methodParams;
	}

	@Override
	public String getMethodString() {
		return null;
	}

	@Override
	public Set<String> getRequestMethods() {
		return requestMethods;
	}

	public MockRequestMapping paths(String... paths) {
		this.paths = paths;
		return this;
	}

	public MockRequestMapping className(String className) {
		this.className = className;
		return this;
	}

	public MockRequestMapping methodName(String methodName) {
		this.methodName = methodName;
		return this;
	}

	public MockRequestMapping methodParams(String... params) {
		this.methodParams = params;
		return this;
	}
}
