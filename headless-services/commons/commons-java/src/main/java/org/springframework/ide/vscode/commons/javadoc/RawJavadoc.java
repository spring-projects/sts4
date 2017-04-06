/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.javadoc;

import org.springframework.ide.vscode.commons.util.Renderable;

public class RawJavadoc implements IJavadoc {
	
	private String rawContent;
	
	public RawJavadoc(String rawContent) {
		this.rawContent = rawContent;
	}

	@Override
	public String raw() {
		return rawContent;
	}

	@Override
	public Renderable getRenderable() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
