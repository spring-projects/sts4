/*******************************************************************************
 * Copyright (c) 2000, 2011, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     IBM Corporation - added J2SE 1.5 support
 *     Pivotal Inc. - copied and modified
 *******************************************************************************/
package org.springframework.ide.vscode.commons.java;

public class Signature {
	
	//These are the bits of JDTs 'Signature' class. Only we copied the method decls
	// of stuff we actually use
	
	//TODO: For performamce reason, JDT probably works with String or even naked char[] instead 
	// of wrapping these in a proper 'TypeSignature' or 'MethodSignature' object. But probably we should
	// do the proper wrapping and abstract out a interface for these types. A 'client' 
	// using 'Java knowledge' should not have to directly be dealing with the JVM method and 
	// type signature strings.
	
	/**
	 * Kind constant for a class type signature.
	 * @see #getTypeSignatureKind(String)
	 * @since 3.0
	 */
	public static final int CLASS_TYPE_SIGNATURE = 1;

	/**
	 * Kind constant for an array type signature.
	 * @see #getTypeSignatureKind(String)
	 * @since 3.0
	 */
	public static final int ARRAY_TYPE_SIGNATURE = 4;

	/**
	 * Character constant indicating the start of an unresolved, named type in a
	 * signature. Value is <code>'Q'</code>.
	 */
	public static final char C_UNRESOLVED = 'Q';
	//TODO: unless we use JDT to work with suource-types we probably don't see these?

	/**
	 * Returns the kind of type signature encoded by the given string.
	 *
	 * @param typeSignature the type signature string
	 * @return the kind of type signature; one of the kind constants:
	 * {@link #ARRAY_TYPE_SIGNATURE}, {@link #CLASS_TYPE_SIGNATURE},
	 * {@link #BASE_TYPE_SIGNATURE}, or {@link #TYPE_VARIABLE_SIGNATURE},
	 * or (since 3.1) {@link #WILDCARD_TYPE_SIGNATURE} or {@link #CAPTURE_TYPE_SIGNATURE}
	 * or (since 3.7) {@link #INTERSECTION_TYPE_SIGNATURE}
	 * @exception IllegalArgumentException if this is not a type signature
	 * @since 3.0
	 */
	public static int getTypeSignatureKind(String typeSignature) {
		return getTypeSignatureKind(typeSignature.toCharArray());
	}

	/**
	 * Returns the kind of type signature encoded by the given string.
	 *
	 * @param typeSignature the type signature string
	 * @return the kind of type signature; one of the kind constants:
	 * {@link #ARRAY_TYPE_SIGNATURE}, {@link #CLASS_TYPE_SIGNATURE},
	 * {@link #BASE_TYPE_SIGNATURE}, or {@link #TYPE_VARIABLE_SIGNATURE},
	 * or (since 3.1) {@link #WILDCARD_TYPE_SIGNATURE} or {@link #CAPTURE_TYPE_SIGNATURE},
	 * or (since 3.7) {@link #INTERSECTION_TYPE_SIGNATURE}
	 * @exception IllegalArgumentException if this is not a type signature
	 * @since 3.0
	 */
	public static int getTypeSignatureKind(char[] typeSignature) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * Extracts the type erasure signature from the given parameterized type signature.
	 * Returns the given type signature if it is not parameterized.
	 *
	 * @param parameterizedTypeSignature the parameterized type signature
	 * @return the signature of the type erasure
	 * @exception IllegalArgumentException if the signature is syntactically
	 *   incorrect
	 *
	 * @since 3.1
	 */
	public static String getTypeErasure(String parameterizedTypeSignature) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * Returns package fragment of a type signature. The package fragment separator must be '.'
	 * and the type fragment separator must be '$'.
	 * <p>
	 * For example:
	 * <pre>
	 * <code>
	 * getSignatureQualifier("Ljava.util.Map$Entry") -> "java.util"
	 * </code>
	 * </pre>
	 * </p>
	 *
	 * @param typeSignature the type signature
	 * @return the package fragment (separators are '.')
	 * @since 3.1
	 */
	public static String getSignatureQualifier(String typeSignature) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * Returns type fragment of a type signature. The package fragment separator must be '.'
	 * and the type fragment separator must be '$'.
	 * <p>
	 * For example:
	 * <pre>
	 * <code>
	 * getSignatureSimpleName("Ljava.util.Map$Entry") -> "Map.Entry"
	 * </code>
	 * </pre>
	 * </p>
	 *
	 * @param typeSignature the type signature
	 * @return the type fragment (separators are '.')
	 * @since 3.1
	 */
	public static String getSignatureSimpleName(String typeSignature) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * Extracts the type parameter signatures from the given method or type signature.
	 * The method or type signature is expected to be dot-based.
	 *
	 * @param methodOrTypeSignature the method or type signature
	 * @return the list of type parameter signatures
	 * @exception IllegalArgumentException if the signature is syntactically
	 *   incorrect
	 *
	 * @since 3.1
	 */
	public static String[] getTypeParameters(String methodOrTypeSignature) throws IllegalArgumentException {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * Extracts the type argument signatures from the given type signature.
	 * Returns an empty array if the type signature is not a parameterized type signature.
	 *
	 * @param parameterizedTypeSignature the parameterized type signature
	 * @return the signatures of the type arguments
	 * @exception IllegalArgumentException if the signature is syntactically incorrect
	 *
	 * @since 3.1
	 */
	public static String[] getTypeArguments(String parameterizedTypeSignature) throws IllegalArgumentException {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * Returns the type signature without any array nesting.
	 * <p>
	 * For example:
	 * <pre>
	 * <code>
	 * getElementType("[[I") --> "I".
	 * </code>
	 * </pre>
	 * </p>
	 *
	 * @param typeSignature the type signature
	 * @return the type signature without arrays
	 * @exception IllegalArgumentException if the signature is not syntactically
	 *   correct
	 */
	public static String getElementType(String typeSignature) throws IllegalArgumentException {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * Returns the array count (array nesting depth) of the given type signature.
	 *
	 * @param typeSignature the type signature
	 * @return the array nesting depth, or 0 if not an array
	 * @exception IllegalArgumentException if the signature is not syntactically
	 *   correct
	 */
	public static int getArrayCount(String typeSignature) throws IllegalArgumentException {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * Returns the number of parameter types in the given method signature.
	 *
	 * @param methodSignature the method signature
	 * @return the number of parameters
	 * @exception IllegalArgumentException if the signature is not syntactically
	 *   correct
	 */
	public static int getParameterCount(String methodSignature) throws IllegalArgumentException {
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
