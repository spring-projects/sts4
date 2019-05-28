/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.jdt.ls.extension;

import org.eclipse.jdt.ls.core.internal.HoverInfoProvider;
import org.springframework.tooling.jdt.ls.commons.Logger;
import org.springframework.tooling.jdt.ls.commons.java.JavaCodeCompletion;
import org.springframework.tooling.jdt.ls.commons.java.JavaData;
import org.springframework.tooling.jdt.ls.commons.java.JavaFluxSearch;
import org.springframework.tooling.jdt.ls.commons.java.TypeHierarchy;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

@SuppressWarnings("restriction")
public class JavaHelpers {
	
	private static final Logger logger = Logger.DEFAULT;
	
	final public static Supplier<JavaData> DATA = Suppliers.memoize(() -> new JavaData(element -> HoverInfoProvider.computeSignature(element).getValue(), logger));
	
	final public static Supplier<JavaFluxSearch> SEARCH = Suppliers.memoize(() -> new JavaFluxSearch(logger, DATA.get()));
	
	final public static Supplier<TypeHierarchy> HIERARCHY = Suppliers.memoize(() -> new TypeHierarchy(logger, DATA.get()));
	
	final public static Supplier<JavaCodeCompletion> CODE_COMPLETIONS = Suppliers.memoize(() -> new JavaCodeCompletion());

}
