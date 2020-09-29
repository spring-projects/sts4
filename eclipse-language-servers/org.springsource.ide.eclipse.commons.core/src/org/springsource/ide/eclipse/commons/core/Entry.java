/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.core;

import java.io.Serializable;

/**
 * @author Andrew Eisenberg
 * @author Christian Dupuis
 * @author Kris De Volder
 * @since 2.5.0
 */
public class Entry implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String command;

	private final String project;

	public Entry(String command, String project) {
		super();
		this.command = command;
		this.project = project;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Entry other = (Entry) obj;
		if (command == null) {
			if (other.command != null) {
				return false;
			}
		}
		else if (!command.equals(other.command)) {
			return false;
		}
		if (project == null) {
			if (other.project != null) {
				return false;
			}
		}
		else if (!project.equals(other.project)) {
			return false;
		}
		return true;
	}

	public String getCommand() {
		return command;
	}

	public String getMenuLabel() {
		return getCommand().trim() + " (" + getProject().trim() + ")";
	}

	public String getProject() {
		return project;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((command == null) ? 0 : command.hashCode());
		result = prime * result + ((project == null) ? 0 : project.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "Entry(" + command + ", " + project + ")";
	}

}
