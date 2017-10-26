/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.boot.app.cli.requestmappings;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.ide.vscode.commons.java.parser.JLRMethodParser;
import org.springframework.ide.vscode.commons.java.parser.JLRMethodParser.JLRMethod;
import org.springframework.ide.vscode.commons.util.Log;

import com.google.common.base.Objects;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public class RequestMappingImpl1 implements RequestMapping {

	private static final Pattern REQUEST_METHODS_PATTERN = Pattern.compile(".*methods=\\[(.*)\\].*");

	/*
There are two styles of entries:

1) key is a 'path' String. May contain patters like "**"
   "/** /favicon.ico":{
      "bean":"faviconHandlerMapping"
   }

2) key is a 'almost json' String
   "{[/bye],methods=[],params=[],headers=[],consumes=[],produces=[],custom=[]}":{
      "bean":"requestMappingHandlerMapping",
      "method":"public java.lang.String demo.MyController.bye()"
   }
	 */

	private JSONObject beanInfo;
	private String pathKey;
	private Supplier<JLRMethod> methodDataSupplier;
	private Supplier<Set<String>> requestMethodsSupplier;
	private Supplier<String> requestPathSupplier;

	public RequestMappingImpl1(String pathKey, JSONObject beanInfo) {
		this.pathKey = pathKey;
		this.beanInfo = beanInfo;
		this.requestMethodsSupplier = Suppliers.memoize(() -> parseRequestMethods());
		this.requestPathSupplier = Suppliers.memoize(() -> parseRequestPath());
		this.methodDataSupplier = Suppliers.memoize(() -> JLRMethodParser.parse(getMethodString()));
	}

	@Override
	public String getPath() {
		return requestPathSupplier.get();
	}

	@Override
	public String toString() {
		return "RequestMapping("+pathKey+")";
	}

	@Override
	public String getFullyQualifiedClassName() {
		JLRMethod m = getMethodData();
		if (m!=null) {
			return m.getFQClassName();
		}
		return null;
	}

	@Override
	public String getMethodName() {
		JLRMethod m = getMethodData();
		if (m!=null) {
			return m.getMethodName();
		}
		return null;
	}

	/**
	 * Returns the raw string found in the requestmapping info. This is a 'toString' value
	 * of java.lang.reflect.Method object.
	 */
	@Override
	public String getMethodString() {
		try {
			if (beanInfo!=null) {
				if (beanInfo.has("method")) {
					return beanInfo.getString("method");
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

	private JLRMethod getMethodData() {
		return methodDataSupplier.get();
	}

	@Override
	public int hashCode() {
		return pathKey.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RequestMappingImpl1 other = (RequestMappingImpl1) obj;
		return Objects.equal(this.pathKey, other.pathKey)
			&& Objects.equal(this.getMethodString(), other.getMethodString());
	}

	protected Set<String> parseRequestMethods() {
		Matcher matcher = REQUEST_METHODS_PATTERN.matcher(pathKey);
		if (matcher.matches()) {
			return Arrays.stream(matcher.group(1).split("\\s*,\\s*")).collect(Collectors.toSet());
		}
		return Collections.emptySet();
	}

	protected String parseRequestPath() {
		if (pathKey.startsWith("{[")) { //Case 2 (see above)
			//An almost json string. Unfortunately not really json so we can't
			//use org.json or jackson Mapper to properly parse this.
			int start = 2; //right after first '['
			int end = pathKey.indexOf(']');
			if (end>=2) {
				return pathKey.substring(start, end);
			}
		}
		//Case 1, or some unanticipated stuff.
		//Assume the key is the path, which is right for Case 1
		// and  probably more useful than null for 'unanticipated stuff'.
		return pathKey;
	}

	@Override
	public Set<String> getRequestMethods() {
		return requestMethodsSupplier.get();
	}

	@Override
	public String[] getSplitPath() {
		String paths = requestPathSupplier.get();
		return Arrays.stream(paths.split("\\|\\|")).map(s -> s.trim()).toArray(String[]::new);
	}

	@Override
	public String[] getMethodParameters() {
		return getMethodData().getParameters();
	}

}