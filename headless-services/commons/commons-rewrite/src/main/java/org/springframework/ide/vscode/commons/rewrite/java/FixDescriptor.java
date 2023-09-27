/*******************************************************************************
 * Copyright (c) 2022, 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.rewrite.java;

import java.util.List;
import java.util.Map;

import org.openrewrite.marker.Range;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;

final public class FixDescriptor {
	
	private String recipeId;
	
	private RecipeScope recipeScope;
	
	private Range rangeScope;
	
	private List<String> docUris;
	
	private Map<String, Object> parameters;
	
	private String label;
	
	private String[] typeStubs;
	
	private boolean preferred;
	
	public FixDescriptor(String recipeId, List<String> docUris, String label) {
		this.recipeId = recipeId;
		this.docUris = docUris;
		this.label = label;
		this.typeStubs = new String[0];
	}
	
	public FixDescriptor withRecipeScope(RecipeScope recipeScope) {
		this.recipeScope = recipeScope;
		return this;
	}

	public FixDescriptor withRangeScope(Range rangeScope) {
		this.rangeScope = rangeScope;
		return this;
	}

	public FixDescriptor withParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
		return this;
	}
	
	public FixDescriptor withTypeStubs(String... typeStubs) {
		this.typeStubs = typeStubs;
		return this;
	}
	
	public FixDescriptor withPreferred(boolean preferred) {
		this.preferred = preferred;
		return this;
	}
	
	public String getRecipeId() {
		return recipeId;
	}

	public RecipeScope getRecipeScope() {
		return recipeScope;
	}

	public Range getRangeScope() {
		return rangeScope;
	}

	public List<String> getDocUris() {
		return docUris;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public String getLabel() {
		return label;
	}

	public String[] getTypeStubs() {
		return typeStubs;
	}
	
	public boolean isPreferred() {
		return preferred;
	}

}
