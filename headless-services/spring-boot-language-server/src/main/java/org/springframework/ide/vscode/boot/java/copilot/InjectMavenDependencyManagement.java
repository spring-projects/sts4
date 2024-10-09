/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.copilot;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Mark Pollack
 */
public class InjectMavenDependencyManagement {

	private String text;

	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public InjectMavenDependencyManagement(@JsonProperty("text") String text) {
		this.text = Objects.requireNonNull(text);
	}

	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		return "InjectMavenDependency{" + "text='" + text + '\'' + '}';
	}

}
