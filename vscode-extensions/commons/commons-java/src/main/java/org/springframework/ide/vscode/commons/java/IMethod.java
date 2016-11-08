package org.springframework.ide.vscode.commons.java;

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

}
