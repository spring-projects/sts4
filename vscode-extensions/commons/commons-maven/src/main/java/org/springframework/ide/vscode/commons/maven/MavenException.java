/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.maven;

/**
 * Wrapper for Maven exceptions
 * 
 * @author Alex Boyko
 *
 */
public class MavenException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private Throwable t;
	
	public MavenException() {
		super();
	}
	
	public MavenException(Throwable t) {
		super();
		this.t = t;
	}

	@Override
	public String getMessage() {
		if (t != null) {
			return t.getMessage();
		}
		return super.getMessage();
	}	

}
