/*******************************************************************************
 * Copyright (c) 2025 Broadcom
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.protocol.spring;

import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolKind;

public class BeanRegistrarElement extends AbstractSpringIndexElement implements SymbolElement {
	
	private final String name;
	private final String type;
	private final Location location;
	
	public BeanRegistrarElement(String name, String type, Location location) {
		this.name = name;
		this.type = type;
		this.location = location;
	}
	
	public String getType() {
		return type;
	}
	
	public Location getLocation() {
		return location;
	}

	@Override
	public DocumentSymbol getDocumentSymbol() {
		return new DocumentSymbol(
				name + " (Bean Registrar)",
				SymbolKind.Class,
				location.getRange(), location.getRange());
	}
	
}
