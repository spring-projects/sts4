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
package org.springframework.ide.vscode.boot.java.requestmapping;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StringLiteral;

/**
 * @author Martin Lippert
 */
public class WebfluxUtils {

	public static final String ROUTER_FUNCTION_TYPE = "org.springframework.web.reactive.function.server.RouterFunction";
	public static final String ROUTER_FUNCTIONS_TYPE = "org.springframework.web.reactive.function.server.RouterFunctions";
	public static final String REQUEST_PREDICATES_TYPE = "org.springframework.web.reactive.function.server.RequestPredicates";
	
	public static final String REQUEST_PREDICATE_PATH_METHOD = "path";
	public static final String REQUEST_PREDICATE_METHOD_METHOD = "method";

	public static final Set<String> REQUEST_PREDICATE_HTTPMETHOD_METHODS = new HashSet<>(Arrays.asList("GET", "POST", "DELETE", "PUT", "PATCH", "HEAD", "OPTIONS"));
	public static final Set<String> REQUEST_PREDICATE_ALL_PATH_METHODS = new HashSet<>(Arrays.asList(REQUEST_PREDICATE_PATH_METHOD, "GET", "POST", "DELETE", "PUT", "PATCH", "HEAD", "OPTIONS"));


	public static String extractPath(MethodInvocation node) {
		List<?> arguments = node.arguments();
		if (arguments != null && arguments.size() > 0) {
			Object object = arguments.get(0);
			if (object instanceof StringLiteral) {
				String path = ((StringLiteral) object).getLiteralValue();
				return path;
			}
		}
		return null;
	}
	
	public static boolean isRouteMethodInvocation(IMethodBinding methodBinding) {
		if (ROUTER_FUNCTIONS_TYPE.equals(methodBinding.getDeclaringClass().getBinaryName())) {
			String name = methodBinding.getName();
			if ("route".equals(name)) {
				return true;
			}
		}
		else if (ROUTER_FUNCTION_TYPE.equals(methodBinding.getDeclaringClass().getBinaryName())) {
			String name = methodBinding.getName();
			if ("andRoute".equals(name)) {
				return true;
			}
		}
		
		return false;
		
	}
	

}
