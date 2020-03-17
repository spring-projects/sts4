/*******************************************************************************
 * Copyright (c) 2000, 2014, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     IBM Corporation - added J2SE 1.5 support
 *     Pivotal Inc - Copied and modified
 *******************************************************************************/
package org.springframework.ide.vscode.commons.java;

import java.util.List;
import java.util.stream.Stream;

public interface IMethod extends IMember {

	/**
	 * Returns the type signature of the return value of this method.
	 * For constructors, this returns the signature for void.
	 * <p>
	 * For example, a source method declared as <code>public String getName()</code>
	 * would return <code>"QString;"</code>.
	 * </p>
	 * <p>
	 * The type signature may be either unresolved (for source types)
	 * or resolved (for binary types), and either basic (for basic types)
	 * or rich (for parameterized types). See {@link Signature} for details.
	 * </p>
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 * @return the type signature of the return value of this method, void  for constructors
	 * @see Signature
	 */
	IJavaType getReturnType();

//	/**
//	 * Returns the signature of this method. This includes the signatures for the
//	 * parameter types and return type, but does not include the method name,
//	 * exception types, or type parameters.
//	 * <p>
//	 * For example, a source method declared as <code>public void foo(String text, int length)</code>
//	 * would return <code>"(QString;I)V"</code>.
//	 * </p>
//	 * <p>
//	 * The type signatures embedded in the method signature may be either unresolved
//	 * (for source types) or resolved (for binary types), and either basic (for
//	 * basic types) or rich (for parameterized types). See {@link Signature} for
//	 * details.
//	 * </p>
//	 *
//	 * @return the signature of this method
//	 * @exception JavaModelException if this element does not exist or if an
//	 *      exception occurs while accessing its corresponding resource.
//	 * @see Signature
//	 */
//	String getSignature();

	/**
	 * Returns parameter types of this method
	 * @return
	 */
	Stream<IJavaType> parameters();

	boolean isConstructor();

	@Override
	default IJavaModuleData classpathContainer() {
		return getDeclaringType().classpathContainer();
	}

	List<String> getParameterNames();

}
