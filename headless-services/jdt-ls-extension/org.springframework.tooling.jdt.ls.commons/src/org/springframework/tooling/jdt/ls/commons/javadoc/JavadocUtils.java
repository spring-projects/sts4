/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.jdt.ls.commons.javadoc;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.function.Function;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ITypeParameter;
import org.springframework.tooling.jdt.ls.commons.resources.ResourceUtils;

import com.google.common.io.CharStreams;

public class JavadocUtils {
	
	@FunctionalInterface
	public interface ReaderProvider {
		Reader getReader(IJavaElement javaElement);
	}
	
	private static String getString(Reader reader) {
		try {
			return CharStreams.toString(reader);
		} catch (IOException ignored) {
			//meh
		}
		return null;
	}

	public static final String javadoc(Function<IJavaElement, Reader> readerProvider, URI projectUri, String bindingKey) throws Exception {
		IJavaProject project = ResourceUtils.getJavaProject(projectUri);
		IJavaElement element = project.findElement(bindingKey, null);
		return computeJavadoc(readerProvider, element);
	}

	private static String computeJavadoc(Function<IJavaElement, Reader> readerProvider, IJavaElement element) {
		if (element == null) {
			return null;
		}
		IMember member;
		if (element instanceof ITypeParameter) {
			member= ((ITypeParameter) element).getDeclaringMember();
		} else if (element instanceof IMember) {
			member= (IMember) element;
		} else if (element instanceof IPackageFragment) {
			Reader r = readerProvider.apply(element);
			if(r == null ) {
				return null;
			}
			return getString(r);
		} else {
			return null;
		}

		Reader r = readerProvider.apply(member);
		if(r == null ) {
			return null;
		}
		return getString(r);
	}

}
