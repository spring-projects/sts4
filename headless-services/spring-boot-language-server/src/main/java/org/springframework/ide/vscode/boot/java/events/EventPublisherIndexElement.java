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
package org.springframework.ide.vscode.boot.java.events;

import java.util.Set;

import org.eclipse.lsp4j.Location;
import org.springframework.ide.vscode.commons.protocol.spring.AbstractSpringIndexElement;

/**
 * @author Martin Lippert
 */
public class EventPublisherIndexElement extends AbstractSpringIndexElement {
	
	private final String eventType;
	private final Location location;
	private final Set<String> eventTypesFromHierarchy;

	public EventPublisherIndexElement(String eventType, Location location, Set<String> eventTypesFromHierarchy) {
		this.eventType = eventType;
		this.location = location;
		this.eventTypesFromHierarchy = eventTypesFromHierarchy;
	}

	public String getEventType() {
		return eventType;
	}
	
	public Location getLocation() {
		return location;
	}

	public Set<String> getEventTypesFromHierarchy() {
		return eventTypesFromHierarchy;
	}

}
