/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.properties.quickfix;

import org.eclipse.lsp4j.Range;

/**
 * Deprecated property quick fix data
 *
 * @author Alex Boyko
 *
 */
public class DeprecatedPropertyData {

	private Range range;
	private String replacement;
	private String uri;

	public DeprecatedPropertyData(String uri, Range range, String replacement) {
		this.setUri(uri);
		this.range = range;
		this.replacement = replacement;
	}

	public Range getRange() {
		return range;
	}

	public void setRange(Range range) {
		this.range = range;
	}

	public String getReplacement() {
		return replacement;
	}

	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

}
