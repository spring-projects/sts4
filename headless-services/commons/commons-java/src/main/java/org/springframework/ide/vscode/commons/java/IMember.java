/*******************************************************************************
 * Copyright (c) 2000, 2013, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Pivotal Inc - Copied and modified
 *******************************************************************************/
package org.springframework.ide.vscode.commons.java;

public interface IMember extends IJavaElement, IAnnotatable {

	/**
	 * Returns the modifier flags for this member. The flags can be examined using class
	 * <code>Flags</code>.
	 * <p>
	 * For {@linkplain #isBinary() binary} members, flags from the class file
	 * as well as derived flags {@link Flags#AccAnnotationDefault} and {@link Flags#AccDefaultMethod} are included.
	 * </p>
	 * <p>
	 * For source members, only flags as indicated in the source are returned. Thus if an interface
	 * defines a method <code>void myMethod();</code>, the flags don't include the
	 * 'public' flag. Source flags include {@link Flags#AccAnnotationDefault} as well.
	 * </p>
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 * @return the modifier flags for this member
	 * @see Flags
	 */
	int getFlags();

	/**
	 * Returns the type in which this member is declared, or <code>null</code>
	 * if this member is not declared in a type (for example, a top-level type).
	 * This is a handle-only method.
	 *
	 * @return the type in which this member is declared, or <code>null</code>
	 * if this member is not declared in a type (for example, a top-level type)
	 */
	IType getDeclaringType();

	IJavaModuleData classpathContainer();

	String signature();

}
