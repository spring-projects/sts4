/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.yaml.snippet;

import java.util.function.Predicate;

import org.springframework.ide.vscode.commons.yaml.schema.DynamicSchemaContext;

public class Snippet {

	private final String name;
	private final String snippet;
	private final Predicate<DynamicSchemaContext> applicability;

	public Snippet(String name, String snippet, Predicate<DynamicSchemaContext> applicability) {
		super();
		this.name = name;
		this.snippet = snippet;
		this.applicability = applicability;
	}
	public String getName() {
		return name;
	}
	public String getSnippet() {
		return snippet;
	}

	@Override
	public String toString() {
		return "Snippet [ name="+name+",\n" +snippet +"\n]";
	}
}
