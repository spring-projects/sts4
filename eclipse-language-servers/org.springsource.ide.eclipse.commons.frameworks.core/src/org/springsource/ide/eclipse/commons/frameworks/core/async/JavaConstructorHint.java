/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.core.async;

import org.eclipse.jdt.internal.compiler.env.AccessRestriction;

@SuppressWarnings("restriction")
/**
 * Contains information required for JDT's BasicSearchEngine to search for constructors. Note that this is to support
 * "legacy" implementation around an internal JDT search engine that allows for searching constructors
 * 
 *
 */
public class JavaConstructorHint {
	public int modifiers;
	public char[] simpleTypeName;
	public int parameterCount;
	public char[] signature;
	public char[][] parameterTypes;
	public char[][] parameterNames;
	public int typeModifiers;
	public char[] packageName;
	public int extraFlags;
	public String path;
	public AccessRestriction access;

	public JavaConstructorHint(int modifiers, char[] simpleTypeName, int parameterCount, char[] signature,
			char[][] parameterTypes, char[][] parameterNames, int typeModifiers, char[] packageName, int extraFlags,
			String path, AccessRestriction access) {
		this.modifiers = modifiers;
		this.simpleTypeName = simpleTypeName;
		this.parameterCount = parameterCount;
		this.signature = signature;
		this.parameterTypes = parameterTypes;
		this.parameterNames = parameterNames;
		this.typeModifiers = typeModifiers;
		this.packageName = packageName;
		this.extraFlags = extraFlags;
		this.path = path;
		this.access = access;
	}

	public static JavaConstructorHint asHint(int modifiers, char[] simpleTypeNames, int parameterCount,
			char[] signatures, char[][] parameterTypes, char[][] parameterNames, int typeModifiers, char[] packageNames,
			int extraFlags, String path, AccessRestriction access) {
		return new JavaConstructorHint(modifiers, simpleTypeNames, parameterCount, signatures, parameterTypes,
				parameterNames, typeModifiers, packageNames, extraFlags, path, access);
	}
}