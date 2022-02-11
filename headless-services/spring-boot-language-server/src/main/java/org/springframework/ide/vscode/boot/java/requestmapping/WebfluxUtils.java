/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.requestmapping;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J.Literal;
import org.openrewrite.java.tree.J.MethodInvocation;
import org.openrewrite.java.tree.JavaType.Method;

/**
 * @author Martin Lippert
 */
public class WebfluxUtils {

	public static final String ROUTER_FUNCTION_TYPE = "org.springframework.web.reactive.function.server.RouterFunction";
	public static final String ROUTER_FUNCTIONS_TYPE = "org.springframework.web.reactive.function.server.RouterFunctions";
	public static final String REQUEST_PREDICATES_TYPE = "org.springframework.web.reactive.function.server.RequestPredicates";
	
	public static final String REQUEST_PREDICATE_PATH_METHOD = "path";
	public static final String REQUEST_PREDICATE_METHOD_METHOD = "method";
	public static final String REQUEST_PREDICATE_ACCEPT_TYPE_METHOD = "accept";
	public static final String REQUEST_PREDICATE_CONTENT_TYPE_METHOD = "contentType";
	public static final String REQUEST_PREDICATE_NEST_METHOD = "nest";

	public static final Set<String> REQUEST_PREDICATE_HTTPMETHOD_METHODS = new HashSet<>(Arrays.asList("GET", "POST", "DELETE", "PUT", "PATCH", "HEAD", "OPTIONS"));
	public static final Set<String> REQUEST_PREDICATE_ALL_PATH_METHODS = new HashSet<>(Arrays.asList(REQUEST_PREDICATE_PATH_METHOD, "GET", "POST", "DELETE", "PUT", "PATCH", "HEAD", "OPTIONS"));


	public static <T> T extractArgument(MethodInvocation node, Class<T> clazz) {
		if (node.getArguments() != null) {
			for (Expression e : node.getArguments()) {
				if (clazz.isInstance(e)) {
					return clazz.cast(e);
				}
			}
		}
		return null;
	}
	
	public static boolean isRouteMethodInvocation(Method method) {
		switch (method.getDeclaringType().getFullyQualifiedName()) {
		case ROUTER_FUNCTIONS_TYPE:
			if ("route".equals(method.getName())) {
				return true;
			}
			break;
		case ROUTER_FUNCTION_TYPE:
			if ("andRoute".equals(method.getName())) {
				return true;
			}
			break;
		}
		return false;
	}
	
	public static String getMediaType(String constantRep) {
		if (constantRep == null) {
			return null;
		}
		
		try {
			if (constantRep.endsWith("_VALUE")) {
				constantRep = constantRep.substring(0, constantRep.lastIndexOf("_VALUE"));
			}
			
			MediaTypeMapping mediaType = MediaTypeMapping.valueOf(constantRep);
			return mediaType.getMediaType();
		}
		catch (IllegalArgumentException e) {
			return constantRep;
		}
	}
	
	public static String getStringRep(String[] multipleTypes, Function<String, String> valueConverter) {
		if (multipleTypes == null || multipleTypes.length == 0) return null;
		
		StringBuilder result = new StringBuilder(valueConverter.apply(multipleTypes[0]));
		for (int i = 1; i < multipleTypes.length; i++) {
			result.append(",");
			result.append(valueConverter.apply(multipleTypes[i]));
		}
		
		return result.toString();
	}


	

}
