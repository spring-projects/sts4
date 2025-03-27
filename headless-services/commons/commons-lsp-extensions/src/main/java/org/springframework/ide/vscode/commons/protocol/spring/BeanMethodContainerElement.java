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

import org.eclipse.lsp4j.Location;

/**
 * This index element is meant to capture bean elements from @Bean annotated
 * methods where the containing class is not a configuration class (e.g. Feign config
 * classes)
 * 
 * This container element is meant for internal use and not to be displayed
 * on the UI, therefore this is not a symbol element.
 * 
 * @author Martin Lippert
 */
public class BeanMethodContainerElement extends AbstractSpringIndexElement {
	
	private final Location location;
	private final String type;
	
	public BeanMethodContainerElement(Location location, String type) {
		super();
		this.location = location;
		this.type = type;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public String getType() {
		return type;
	}
	
}
