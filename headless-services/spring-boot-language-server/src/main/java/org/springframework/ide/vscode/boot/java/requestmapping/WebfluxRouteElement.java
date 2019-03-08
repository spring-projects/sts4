/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.requestmapping;

import org.eclipse.lsp4j.Range;

/**
 * @author Martin Lippert
 */
public class WebfluxRouteElement {
	
	private String element;
	private Range elementRange;
	
	public WebfluxRouteElement(String element, Range elementRange) {
		super();
		this.element = element;
		this.elementRange = elementRange;
	}
	
	public String getElement() {
		return element;
	}
	
	public Range getElementRange() {
		return elementRange;
	}
	
}
