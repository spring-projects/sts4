/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.java;

import java.util.stream.Stream;

public interface IAnnotation extends IJavaElement {

	/**
	 * Returns the member-value pairs of this annotation. Returns an empty
	 * array if this annotation is a marker annotation. Returns a size-1 array if this
	 * annotation is a single member annotation. In this case, the member
	 * name is always <code>"value"</code>.
	 *
	 * @return the member-value pairs of this annotation
	 */
	Stream<IMemberValuePair> getMemberValuePairs();

	String fqName();

}
