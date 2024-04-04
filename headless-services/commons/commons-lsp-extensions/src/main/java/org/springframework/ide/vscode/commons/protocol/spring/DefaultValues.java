/*******************************************************************************
 * Copyright (c) 2023, 2024 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.protocol.spring;

import java.util.HashSet;
import java.util.Set;

public class DefaultValues {

	public static final Set<String> EMPTY_SUPERTYPES = new HashSet<>();
	public static final Set<String> OBJECT_SUPERTYPE = Set.of("java.lang.Object");
	
	public static final InjectionPoint[] EMPTY_INJECTION_POINTS = new InjectionPoint[0];

}
