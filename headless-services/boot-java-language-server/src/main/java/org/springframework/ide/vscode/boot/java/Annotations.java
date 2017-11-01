/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java;

/**
 * Constants containing various fully-qualified annotation names.
 *
 * @author Kris De Volder
 */
public class Annotations {
	public static final String BEAN = "org.springframework.context.annotation.Bean";
	public static final String PROFILE = "org.springframework.context.annotation.Profile";
	public static final String COMPONENT = "org.springframework.stereotype.Component";
	public static final String AUTOWIRED = "org.springframework.beans.factory.annotation.Autowired";
}
