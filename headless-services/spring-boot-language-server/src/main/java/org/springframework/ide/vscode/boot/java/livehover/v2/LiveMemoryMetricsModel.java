/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover.v2;

/**
 * @author Udayani V
 */
public class LiveMemoryMetricsModel {

	private String name;
	private String description;
	private Measurements[] measurements;
	private String baseUnit;
	private AvailableTags[] availableTags;
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public Measurements[] getMeasurements() {
		return measurements;
	}
	
	public String getBaseUnit() {
		return baseUnit;
	}
	
	public AvailableTags[] getAvailableTags() {
		return availableTags;
	}
	
	
}
